"""Run only banking requests through Bruno; preserve planning data and review decisions."""
import argparse
import datetime as dt
import json
import os
from pathlib import Path
import subprocess
import tempfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--env", default="Prod")
    parser.add_argument("--from", dest="date_from", required=True, type=dt.date.fromisoformat)
    parser.add_argument("--to", dest="date_to", default=dt.date.today(), type=dt.date.fromisoformat)
    parser.add_argument("--connection", help="Limit to one existing connection UUID")
    parser.add_argument("--expected-accounts", type=int)
    parser.add_argument("--verify-repeat", action="store_true", help="Repeat each sync and check transaction IDs remain stable")
    args = parser.parse_args()
    if args.date_from > args.date_to:
        parser.error("--from must not be after --to")
    root = Path(__file__).resolve().parent
    reports = root / "reports"
    reports.mkdir(exist_ok=True)
    npx = "npx.cmd" if os.name == "nt" else "npx"

    def request(filename, **variables):
        with tempfile.TemporaryDirectory(prefix="plannr-banking-") as temporary:
            output = Path(temporary) / "result.json"
            command = [npx, "--yes", "@usebruno/cli@4.2.1", "run", filename,
                       "--env", args.env, "--output", str(output), "--format", "json"]
            for name, value in variables.items():
                command += ["--env-var", f"{name}={value}"]
            completed = subprocess.run(command, cwd=root, capture_output=True, text=True, encoding="utf-8", errors="replace")
            if not output.exists():
                raise RuntimeError(f"Bruno could not run {filename}: {completed.stderr}")
            result = json.loads(output.read_text(encoding="utf-8"))[0]["results"][0]
            response = result.get("response") or {}
            if completed.returncode or response.get("status") != 200:
                data = response.get("data")
                code = data.get("error", {}).get("code") if isinstance(data, dict) else None
                raise RuntimeError(f"{filename}: HTTP {response.get('status')}, code={code or 'unknown'}")
            return response["data"]

    connections = request("01-List-connections.bru")
    selected = [c for c in connections if c["status"] == "ACTIVE"
                and (not args.connection or c["id"] == args.connection)]
    if not selected:
        raise RuntimeError("No matching active connection; authorize or renew consent first")
    for connection in selected:
        refreshed = request("02-Refresh-connection.bru", connectionId=connection["id"])
        if refreshed["status"] != "ACTIVE":
            raise RuntimeError("Consent is no longer active; renew it before importing")
    selected_ids = {c["id"] for c in selected}
    accounts = [a for a in request("03-List-bank-accounts.bru") if a["connectionId"] in selected_ids]
    if not accounts or (args.expected_accounts is not None and len(accounts) != args.expected_accounts):
        raise RuntimeError(f"Discovered {len(accounts)} accounts; expected {args.expected_accounts or 'at least one'}")

    def transaction_ids(account_id):
        ids = set()
        offset = 0
        while True:
            page = request("05-List-transactions.bru", bankAccountId=account_id, limit=500, offset=offset)
            ids.update(t["id"] for t in page)
            if len(page) < 500:
                return ids
            offset += len(page)

    summary = {"dateFrom": str(args.date_from), "dateTo": str(args.date_to), "accounts": []}
    for account in accounts:
        cursor = args.date_to
        observations = 0
        while cursor >= args.date_from:
            start = max(cursor - dt.timedelta(days=364), args.date_from)
            variables = dict(bankAccountId=account["id"], dateFrom=start, dateTo=cursor)
            synced = request("04-Sync-account.bru", **variables)
            observations += synced["imported"]
            if args.verify_repeat:
                before = transaction_ids(account["id"])
                request("04-Sync-account.bru", **variables)
                after = transaction_ids(account["id"])
                if before != after:
                    raise RuntimeError(f"Account {account['id']}: transaction IDs changed on repeat; review bank observations")
            print(f"Account {account['id']}: synced {start} through {cursor} ({synced['imported']} observations)", flush=True)
            cursor = start - dt.timedelta(days=1)
        ids = transaction_ids(account["id"])
        item = {"bankAccountId": account["id"], "planningAccountId": account["accountId"],
                "observationsReceived": observations, "storedTransactions": len(ids)}
        summary["accounts"].append(item)
        print(f"Account {account['id']}: {len(ids)} stored transactions")
        (reports / "import-summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
    print(f"Completed {len(accounts)} accounts; summary: {reports / 'import-summary.json'}")


if __name__ == "__main__":
    main()
