const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

function readBruJsonBody(fileName) {
  const source = fs.readFileSync(
    path.resolve(__dirname, "../Seed/transaction templates", fileName),
    "utf8"
  );
  const match = source.match(
    /body:json\s*\{\s*(\{[\s\S]*\})\s*\}\s*settings\s*\{/
  );

  if (!match) {
    throw new Error(`Could not extract JSON body from ${fileName}`);
  }

  return JSON.parse(match[1]);
}

test("Rainy Day Fund seeds the legacy balance of EUR 5,734", () => {
  const body = readBruJsonBody("Rainy Day Fund.bru");
  const incoming = body.templates.filter(
    (template) =>
      template.transactionType === "TRANSFER" &&
      template.destinationPocketId === "{{pocket_rainy_day_fund}}"
  );
  const expenses = body.templates.filter(
    (template) =>
      template.transactionType === "EXPENSE" &&
      template.sourcePocketId === "{{pocket_rainy_day_fund}}"
  );

  assert.deepEqual(
    incoming.flatMap((template) => template.versions).map((version) => ({
      amount: version.amount,
      firstOccurrenceDate: version.firstOccurrenceDate,
      recurrenceType: version.recurrenceType,
    })),
    [
      { amount: 30000, firstOccurrenceDate: "2024-11-01", recurrenceType: "MONTHLY" },
      { amount: 1415, firstOccurrenceDate: "2025-09-01", recurrenceType: "NONE" },
      { amount: 83585, firstOccurrenceDate: "2025-09-26", recurrenceType: "NONE" },
      { amount: 190000, firstOccurrenceDate: "2026-06-19", recurrenceType: "NONE" },
    ]
  );
  assert.deepEqual(
    expenses.flatMap((template) => template.versions).map((version) => ({
      amount: version.amount,
      firstOccurrenceDate: version.firstOccurrenceDate,
    })),
    [
      { amount: 30000, firstOccurrenceDate: "2026-02-23" },
      { amount: 1600, firstOccurrenceDate: "2026-06-19" },
    ]
  );
});

test("Catalina uses uninterrupted monthly contributions", () => {
  const body = readBruJsonBody("Catalina.bru");
  assert.deepEqual(body.templates[0].versions, [
    {
      amount: 5000,
      firstOccurrenceDate: "2024-11-01",
      finalOccurrenceDate: null,
      recurrenceType: "MONTHLY",
      skipCount: 0,
      daysOfWeek: null,
      weeksOfMonth: null,
      daysOfMonth: [1],
      monthsOfYear: null,
      maxRecurrenceCount: null,
    },
  ]);

  const gifts = body.templates.find(
    (template) => template.transactionType === "INCOME"
  );
  const expenses = body.templates.find(
    (template) => template.transactionType === "EXPENSE"
  );
  assert.deepEqual(
    gifts.versions.map((version) => [
      version.amount,
      version.firstOccurrenceDate,
    ]),
    [
      [37500, "2025-10-15"],
      [10000, "2026-04-16"],
      [7000, "2026-05-20"],
      [10000, "2026-06-25"],
      [10000, "2026-09-07"],
    ]
  );
  assert.deepEqual(
    expenses.versions.map((version) => [
      version.amount,
      version.firstOccurrenceDate,
    ]),
    [
      [20000, "2025-10-21"],
      [16000, "2025-10-31"],
      [15000, "2025-11-10"],
      [10000, "2025-11-29"],
      [20000, "2026-02-11"],
    ]
  );
});

test("Hausrat includes the July 2026 rate-adjustment top-up", () => {
  const body = readBruJsonBody("Hausratversicherung.bru");
  const savings = body.templates.find(
    (template) =>
      template.transactionType === "TRANSFER" &&
      template.versions.some((version) => version.recurrenceType === "MONTHLY")
  );
  const topUp = body.templates.find((template) =>
    template.versions.some((version) => version.amount === 1045)
  );

  assert.deepEqual(
    savings.versions.map((version) => ({
      amount: version.amount,
      firstOccurrenceDate: version.firstOccurrenceDate,
      recurrenceType: version.recurrenceType,
    })),
    [
      { amount: 847, firstOccurrenceDate: "2025-08-01", recurrenceType: "MONTHLY" },
    ]
  );
  assert.ok(topUp);
  assert.equal(topUp.transactionType, "TRANSFER");
  assert.equal(topUp.sourcePocketId, "{{pocket_income_bucket}}");
  assert.equal(topUp.destinationPocketId, "{{pocket_hausratversicherung}}");
  assert.equal(topUp.versions[0].firstOccurrenceDate, "2026-07-01");
  assert.equal(topUp.versions[0].recurrenceType, "NONE");

  const expense = body.templates.find(
    (template) => template.transactionType === "EXPENSE"
  );
  assert.deepEqual(
    expense.versions.map((version) => [
      version.amount,
      version.firstOccurrenceDate,
    ]),
    [[10162, "2026-10-16"]]
  );
});

test("the September Growth Fund contribution remains in Insurance", () => {
  const body = readBruJsonBody("Catalina Growth Fund.bru");
  const contributionToInsurance = body.templates.find(
    (template) =>
      template.sourcePocketId === "{{pocket_income_bucket}}" &&
      template.destinationPocketId ===
        "{{pocket_catalina_growth_fund_insurance_and_contracts}}"
  );
  const transferToTradeRepublic = body.templates.find(
    (template) =>
      template.sourcePocketId ===
        "{{pocket_catalina_growth_fund_insurance_and_contracts}}" &&
      template.destinationPocketId ===
        "{{pocket_catalina_growth_fund_trade_republic}}"
  );

  assert.deepEqual(
    contributionToInsurance.versions.map((version) => ({
      amount: version.amount,
      firstOccurrenceDate: version.firstOccurrenceDate,
      recurrenceType: version.recurrenceType,
    })),
    [
      { amount: 25900, firstOccurrenceDate: "2026-09-01", recurrenceType: "NONE" },
      { amount: 25900, firstOccurrenceDate: "2026-10-01", recurrenceType: "MONTHLY" },
    ]
  );
  assert.deepEqual(
    transferToTradeRepublic.versions.map((version) => ({
      amount: version.amount,
      firstOccurrenceDate: version.firstOccurrenceDate,
      recurrenceType: version.recurrenceType,
    })),
    [
      { amount: 25900, firstOccurrenceDate: "2026-10-01", recurrenceType: "MONTHLY" },
    ]
  );
});

test("Rechtsschutz and chennemann.de retain the approved rate dates", () => {
  const rechtsschutz = readBruJsonBody("Rechtsschutzversicherung.bru");
  const previousRate = rechtsschutz.templates[0].versions.find(
    (version) => version.amount === 2225
  );
  const newRate = rechtsschutz.templates[0].versions.find(
    (version) => version.amount === 2518
  );
  assert.equal(previousRate.firstOccurrenceDate, "2024-03-01");
  assert.equal(newRate.firstOccurrenceDate, "2025-03-01");

  const domain = readBruJsonBody("chennemann.de.bru");
  const yearlyFee = domain.templates
    .flatMap((template) => template.versions)
    .find((version) => version.amount === 960);
  assert.equal(yearlyFee.firstOccurrenceDate, "2026-07-15");
  assert.equal(yearlyFee.recurrenceType, "YEARLY");
});

test("approved historical savings start on their backup dates", () => {
  const expectedStarts = new Map([
    ["AXA Leben.bru", [4144, "2025-09-01"]],
    ["Amazon Prime.bru", [750, "2024-01-01"]],
    ["Auto Apps.bru", [88, "2025-05-01"]],
    ["Duolingo.bru", [1025, "2024-11-01"]],
    ["Netflix.bru", [500, "2024-10-01"]],
    ["Kfz-Steuer.bru", [900, "2024-10-01"]],
    ["Kfz-Versicherung.bru", [4594, "2024-11-01"]],
    ["MagentaMobil.bru", [2995, "2025-09-01"]],
    ["Rundfunkbeitrag.bru", [1836, "2024-09-01"]],
    ["Swiss Life.bru", [11242, "2025-09-01"]],
    ["The System Vault.bru", [500, "2025-09-01"]],
    ["WWK.bru", [5500, "2025-09-01"]],
    ["Ziarul de Garde.bru", [298, "2025-09-01"]],
    ["o2.bru", [999, "2025-09-01"]],
  ]);

  for (const [fileName, [amount, expectedStart]] of expectedStarts) {
    const body = readBruJsonBody(fileName);
    const version = body.templates
      .filter(
        (template) =>
          template.transactionType === "TRANSFER" &&
          template.sourcePocketId === "{{pocket_income_bucket}}"
      )
      .flatMap((template) => template.versions)
      .find(
        (candidate) =>
          candidate.amount === amount && candidate.recurrenceType === "MONTHLY"
      );

    assert.ok(version, fileName);
    assert.equal(version.firstOccurrenceDate, expectedStart, fileName);
  }
});

test("approved historical one-off adjustments are seeded", () => {
  const expectedVersions = [
    ["Auto Apps.bru", "EXPENSE", 1050, "2026-05-01", "YEARLY"],
    ["Kfz-Steuer.bru", "TRANSFER", 10800, "2024-11-28"],
    ["Kfz-Versicherung.bru", "TRANSFER", 55128, "2024-11-28"],
    ["Auto Wartung und Reparatur.bru", "EXPENSE", 1485, "2026-07-18"],
    ["Smart Launcher.bru", "EXPENSE", 949, "2026-06-19"],
  ];

  for (const [fileName, transactionType, amount, expectedDate, recurrenceType = "NONE"] of expectedVersions) {
    const body = readBruJsonBody(fileName);
    const version = body.templates
      .filter((template) => template.transactionType === transactionType)
      .flatMap((template) => template.versions)
      .find(
        (candidate) =>
          candidate.amount === amount &&
          candidate.firstOccurrenceDate === expectedDate &&
          candidate.recurrenceType === recurrenceType
      );

    assert.ok(version, fileName);
  }
});

test("Continentale uses the September savings and October premium rate", () => {
  const body = readBruJsonBody("Continentale.bru");
  const savings = body.templates.find(
    (template) => template.transactionType === "TRANSFER"
  );
  const premiums = body.templates.find(
    (template) => template.transactionType === "EXPENSE"
  );

  assert.deepEqual(
    savings.versions.map((version) => [
      version.amount,
      version.firstOccurrenceDate,
      version.finalOccurrenceDate,
    ]),
    [
      [8442, "2025-10-01", "2026-08-01"],
      [8695, "2026-09-01", null],
    ]
  );
  assert.deepEqual(
    premiums.versions.map((version) => [
      version.amount,
      version.firstOccurrenceDate,
      version.finalOccurrenceDate,
    ]),
    [
      [8442, "2025-11-01", "2026-09-01"],
      [8695, "2026-10-01", null],
    ]
  );
});

test("Fitness First Linka retains the six-payment break", () => {
  const body = readBruJsonBody("Fitness First.bru");
  const linkaMembership = body.templates.find(
    (template) =>
      template.contractId === "{{contract_fitness_first_red_linka}}" &&
      template.title === "Mitgliedsbeitrag"
  );

  assert.deepEqual(
    linkaMembership.versions.map((version) => [
      version.amount,
      version.firstOccurrenceDate,
      version.finalOccurrenceDate,
    ]),
    [
      [1090, "2024-11-12", "2024-12-27"],
      [2180, "2025-01-07", "2025-05-13"],
      [2180, "2025-08-19", null],
    ]
  );
});
