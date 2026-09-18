const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

function readBruJsonBody(relativePath) {
  const source = fs.readFileSync(path.resolve(__dirname, relativePath), "utf8");
  const match = source.match(/body:json\s*\{\s*(\{[\s\S]*\})\s*\}\s*$/);

  if (!match) {
    throw new Error(`Could not extract JSON body from ${relativePath}`);
  }

  return JSON.parse(match[1]);
}

test("non-accumulating seed contracts do not request dedicated pockets", () => {
  const contractsDirectory = path.resolve(__dirname, "../Seed/contracts");
  const contractFiles = fs
    .readdirSync(contractsDirectory)
    .filter(
      (fileName) => fileName.endsWith(".bru") && fileName !== "folder.bru"
    );

  for (const fileName of contractFiles) {
    const relativePath = path.join("../Seed/contracts", fileName);
    const body = readBruJsonBody(relativePath);

    if (body.type === "NON_ACCUMULATING") {
      assert.deepEqual(body.accountIds, [], fileName);
    }
  }
});
