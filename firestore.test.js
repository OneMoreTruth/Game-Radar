const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- SECURITY BOUNDS TESTS ---

test("Unauthenticated user: cannot read wishlist", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("wishlist").get());
});

test("Unauthenticated user: cannot write wishlist item", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("wishlist").doc("game1").set({
      gameId: "game1",
      userId: ALICE_UID,
      title: "Test Game",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot read another user's wishlist", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("wishlist").doc("game1").set({
      gameId: "game1",
      userId: BOB_UID,
      title: "Bob's Game",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("wishlist").doc("game1").get());
});

test("Authenticated user: cannot write to another user's wishlist", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("wishlist").doc("game1").set({
      gameId: "game1",
      userId: ALICE_UID,
      title: "Hacked Game",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can create and read their own wishlist item", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("wishlist").doc("game_witcher").set({
      gameId: "game_witcher",
      userId: ALICE_UID,
      title: "The Witcher 3",
      currentPrice: 14.99,
      targetPrice: 9.99,
      priority: "HIGH",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  const doc = await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("wishlist").doc("game_witcher").get()
  );
  if (!doc.exists) {
    throw new Error("Expected document to exist");
  }
});

test("Validation: rejects invalid priority enum", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("wishlist").doc("game_bad").set({
      gameId: "game_bad",
      userId: ALICE_UID,
      title: "Bad Game",
      priority: "SUPER_URGENT", // invalid enum
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Validation: rejects negative price", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("wishlist").doc("game_neg").set({
      gameId: "game_neg",
      userId: ALICE_UID,
      title: "Negative Price Game",
      currentPrice: -5.0,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can query their own wishlist collection", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("wishlist").doc("game1").set({
      gameId: "game1",
      userId: ALICE_UID,
      title: "Game 1",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("wishlist").get());
});
