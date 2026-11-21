const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_arpan_123";
const BOB_UID = "bob_friend_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validFounderPayload(uid, timestamp = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    name: "Arpan",
    school: "Horizon Science Academy",
    bloodGroup: "O+",
    aimInLife: "To build inspiring software and celebrate lifelong friendships.",
    city: "Kolkata",
    hobby: "UI Design & Stargazing",
    motto: "Friends turn ordinary moments into golden trophies.",
    goodFriends: ["Rohan", "Ananya", "Kabir"],
    themeId: "lavender",
    createdAt: timestamp,
    updatedAt: timestamp,
  };
}

function validFriendPayload(uid, timestamp = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    name: "Rohan Sen",
    nickname: "Spark",
    school: "Horizon Science Academy",
    bloodGroup: "B+",
    city: "Kolkata",
    hobby: "Astrophotography",
    aimInLife: "Explore space engineering and launch nanosatellites.",
    note: "Always brings the best energy to late-night study sessions.",
    avatarPreset: "starlight",
    trophyId: "TRP-1001",
    trophyTitle: "Golden Circle Honoree",
    closeFriend: true,
    cheersCount: 0,
    createdAt: timestamp,
    updatedAt: timestamp,
  };
}

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

test("Unauthenticated user cannot read or write founder_profile or friends", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();

  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("founder_profile")
      .doc("main")
      .get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("founder_profile")
      .doc("main")
      .set(validFounderPayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("friends").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("friends")
      .doc("friend_1")
      .set(validFriendPayload(ALICE_UID))
  );
});

test("Authenticated owner can create, read, update details, switch theme, and delete founder_profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("founder_profile")
    .doc("main");

  await assertSucceeds(profileRef.set(validFounderPayload(ALICE_UID)));
  await assertSucceeds(profileRef.get());

  // Action: Update Founder Details
  await assertSucceeds(
    profileRef.update({
      name: "Arpan Das",
      school: "Starlight Institute",
      bloodGroup: "A+",
      aimInLife: "Build soulful apps and cherish great friends.",
      city: "Bengaluru",
      hobby: "Music & Coding",
      motto: "Shine together.",
      goodFriends: ["Rohan", "Meera"],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Action: Switch Glassmorphic Theme
  await assertSucceeds(
    profileRef.update({
      themeId: "aurora",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject invalid theme
  await assertFails(
    profileRef.update({
      themeId: "neon_invalid",
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    profileRef.update({
      createdAt: new Date(Date.now() - 100),
      updatedAt: new Date(Date.now() - 100),
    })
  );

  // Reject ghost field
  await assertFails(
    profileRef.update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 100),
    })
  );

  await assertSucceeds(profileRef.delete());
});

test("Authenticated owner can create, list, edit, toggle closeFriend, cheer trophy, and delete friends", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const friendsCol = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("friends");
  const friendRef = friendsCol.doc("friend_rohan");

  // Empty subcollection list query succeeds (Kotlin observeFriends parity)
  await assertSucceeds(friendsCol.get());

  // Create valid friend (NO @DocumentId `id` field)
  await assertSucceeds(friendRef.set(validFriendPayload(ALICE_UID)));
  await assertSucceeds(friendsCol.get());

  // Action: Edit Friend Profile Details
  await assertSucceeds(
    friendRef.update({
      name: "Rohan Sen",
      nickname: "Astro",
      school: "Horizon Science Academy",
      bloodGroup: "B+",
      city: "Pune",
      hobby: "Robotics",
      aimInLife: "Build autonomous rovers for lunar exploration.",
      note: "Best robotics teammate ever.",
      avatarPreset: "ocean",
      trophyTitle: "Starlight Ally",
      closeFriend: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Action: Toggle Close Friend Status
  await assertSucceeds(
    friendRef.update({
      closeFriend: true,
      updatedAt: new Date(Date.now() - 800),
    })
  );

  // Action: Celebrate / Cheer Trophy (+1)
  await assertSucceeds(
    friendRef.update({
      cheersCount: 1,
      updatedAt: new Date(Date.now() - 600),
    })
  );

  // Reject invalid cheersCount jump (+5)
  await assertFails(
    friendRef.update({
      cheersCount: 6,
      updatedAt: new Date(Date.now() - 400),
    })
  );

  // Reject modifying immutable trophyId
  await assertFails(
    friendRef.update({
      trophyId: "TRP-9999",
      updatedAt: new Date(Date.now() - 300),
    })
  );

  // Reject @DocumentId `id` field in payload on create
  await assertFails(
    friendsCol.doc("friend_bad_id").set({
      ...validFriendPayload(ALICE_UID),
      id: "friend_bad_id",
    })
  );

  // Reject tampered future timestamp
  await assertFails(
    friendsCol
      .doc("friend_future")
      .set(validFriendPayload(ALICE_UID, new Date(Date.now() + 60000)))
  );

  await assertSucceeds(friendRef.delete());
});

test("Cross-user isolation: Alice cannot read, list, create, update, or delete Bob's data", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb
      .collection("users")
      .doc(BOB_UID)
      .collection("founder_profile")
      .doc("main")
      .set(validFounderPayload(BOB_UID));
    await adminDb
      .collection("users")
      .doc(BOB_UID)
      .collection("friends")
      .doc("bob_friend_1")
      .set(validFriendPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("founder_profile")
      .doc("main")
      .get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("friends").get()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("friends")
      .doc("bob_friend_1")
      .get()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("friends")
      .doc("bob_friend_1")
      .update({
        closeFriend: false,
        updatedAt: new Date(Date.now() - 500),
      })
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("friends")
      .doc("bob_friend_1")
      .delete()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("friends")
      .doc("spoofed")
      .set(validFriendPayload(ALICE_UID))
  );
});
