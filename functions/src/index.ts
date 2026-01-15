/* eslint-disable */

import { onCall, HttpsError, CallableRequest } from "firebase-functions/v2/https";
import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";

initializeApp();
const db = getFirestore();

const REGION = "europe-southwest1";

/* =========================
   Models
========================= */

interface Plant {
  id?: string;
  common_name: string;
  scientific_name: string[];
  plant_name?: string;
  family?: string;
  type?: string;
  image_url?: string;
  care_level?: string;
  sunlight?: string[];
  watering?: string;
  indoor?: boolean;
  poisonous_to_humans?: boolean;
  poisonous_to_pets?: boolean;
  drought_tolerant?: boolean;
  soil?: string[];
  notes?: string;
  ownerId: string;
}

/* =========================
   Helpers
========================= */

function assertAuthenticated(request: CallableRequest): string {
  if (!request.auth) {
    throw new HttpsError(
      "unauthenticated",
      "The function must be called while authenticated."
    );
  }
  return request.auth.uid;
}

/* =========================
   1. Create Plant
========================= */

export const createPlant = onCall(
  { region: REGION },
  async (request) => {
    const userId = assertAuthenticated(request);
    const data = request.data as Partial<Plant>;

    if (!data.common_name || !data.scientific_name) {
      throw new HttpsError(
        "invalid-argument",
        "Missing required fields: common_name, scientific_name"
      );
    }

    const docRef = await db
      .collection("users")
      .doc(userId)
      .collection("plants")
      .add({
        ...data,
        ownerId: userId,
      });

    return { id: docRef.id };
  }
);

/* =========================
   2. Get Plants
========================= */

export const getPlants = onCall(
  { region: REGION },
  async (request) => {
    const userId = assertAuthenticated(request);

    try {
      const snapshot = await db
        .collection("users")
        .doc(userId)
        .collection("plants")
        .get();

      if (snapshot.empty) {
        return [];
      }

      return snapshot.docs.map((doc) => {
        const data = doc.data() as Plant;
        return {
          id: doc.id,
          ...data,
          scientific_name: data.scientific_name ?? [],
          sunlight: data.sunlight ?? [],
          soil: data.soil ?? [],
        };
      });
    } catch (error: unknown) {
      console.error("Error fetching plants:", error);

      const message =
        error instanceof Error ? error.message : "Unknown error";

      throw new HttpsError(
        "internal",
        `Error fetching plants: ${message}`
      );
    }
  }
);

/* =========================
   3. Update Plant
========================= */

export const updatePlant = onCall(
  { region: REGION },
  async (request) => {
    const userId = assertAuthenticated(request);
    const data = request.data as {
      id: string;
      updates: Partial<Omit<Plant, "id" | "ownerId">>;
    };

    if (!data.id) {
      throw new HttpsError("invalid-argument", "Plant ID is required.");
    }

    if (!data.updates || Object.keys(data.updates).length === 0) {
      throw new HttpsError(
        "invalid-argument",
        "No update data provided."
      );
    }

    if ("ownerId" in data.updates) {
      throw new HttpsError(
        "permission-denied",
        "Cannot change plant owner."
      );
    }

    try {
      const plantRef = db
        .collection("users")
        .doc(userId)
        .collection("plants")
        .doc(data.id);

      const plantDoc = await plantRef.get();

      if (!plantDoc.exists) {
        throw new HttpsError("not-found", "Plant not found.");
      }

      const plant = plantDoc.data() as Plant;

      if (plant.ownerId !== userId) {
        throw new HttpsError(
          "permission-denied",
          "You do not have permission to update this plant."
        );
      }

      await plantRef.update(data.updates);

      return { message: "Plant updated successfully!" };
    } catch (error: unknown) {
      console.error("Error updating plant:", error);

      if (error instanceof HttpsError) {
        throw error;
      }

      const message =
        error instanceof Error ? error.message : "Unknown error";

      throw new HttpsError(
        "internal",
        `Error updating plant: ${message}`
      );
    }
  }
);

/* =========================
   4. Delete Plant
========================= */

export const deletePlant = onCall(
  { region: REGION },
  async (request) => {
    const userId = assertAuthenticated(request);
    const data = request.data as { id: string };

    if (!data.id) {
      throw new HttpsError("invalid-argument", "Plant ID is required.");
    }

    try {
      const plantRef = db
        .collection("users")
        .doc(userId)
        .collection("plants")
        .doc(data.id);

      const plantDoc = await plantRef.get();

      if (!plantDoc.exists) {
        throw new HttpsError("not-found", "Plant not found.");
      }

      const plant = plantDoc.data() as Plant;

      if (plant.ownerId !== userId) {
        throw new HttpsError(
          "permission-denied",
          "You do not have permission to delete this plant."
        );
      }

      await plantRef.delete();

      return { message: "Plant deleted successfully!" };
    } catch (error: unknown) {
      console.error("Error deleting plant:", error);

      if (error instanceof HttpsError) {
        throw error;
      }

      const message =
        error instanceof Error ? error.message : "Unknown error";

      throw new HttpsError(
        "internal",
        `Error deleting plant: ${message}`
      );
    }
  }
);
/* =========================
   5. Initialize Sample Plants
========================= */

export const initializeSamplePlants = onCall(
  { region: REGION },
  async (request) => {
    const userId = assertAuthenticated(request);

    try {
      // Check if user already has plants
      const existingPlants = await db
        .collection("users")
        .doc(userId)
        .collection("plants")
        .limit(1)
        .get();

      if (!existingPlants.empty) {
        return {
          message: "User already has plants. Initialization skipped.",
          plantsAdded: 0
        };
      }

      // Sample plants to add
      const samplePlants: Omit<Plant, "id">[] = [
        {
          common_name: "Snake Plant",
          scientific_name: ["Dracaena trifasciata", "Sansevieria trifasciata"],
          family: "Asparagaceae",
          type: "Succulent",
          care_level: "Low",
          sunlight: ["Low", "Part shade"],
          watering: "Minimum",
          indoor: true,
          poisonous_to_humans: false,
          poisonous_to_pets: true,
          drought_tolerant: true,
          soil: ["Well-drained", "Sandy"],
          notes: "Very hardy, perfect for beginners. Can survive in low light.",
          ownerId: userId,
        },
        {
          common_name: "Pothos",
          scientific_name: ["Epipremnum aureum"],
          family: "Araceae",
          type: "Vine",
          care_level: "Low",
          sunlight: ["Low", "Part shade", "Part sun"],
          watering: "Average",
          indoor: true,
          poisonous_to_humans: true,
          poisonous_to_pets: true,
          drought_tolerant: false,
          soil: ["Well-drained", "Loamy"],
          notes: "Fast-growing trailing plant. Easy to propagate from cuttings.",
          ownerId: userId,
        },
        {
          common_name: "Monstera",
          scientific_name: ["Monstera deliciosa"],
          family: "Araceae",
          type: "Vine",
          care_level: "Moderate",
          sunlight: ["Part shade", "Part sun"],
          watering: "Average",
          indoor: true,
          poisonous_to_humans: true,
          poisonous_to_pets: true,
          drought_tolerant: false,
          soil: ["Well-drained", "Loamy", "Peat moss"],
          notes: "Popular houseplant with iconic split leaves. Needs support as it grows.",
          ownerId: userId,
        },
        {
          common_name: "Spider Plant",
          scientific_name: ["Chlorophytum comosum"],
          family: "Asparagaceae",
          type: "Perennial",
          care_level: "Low",
          sunlight: ["Part shade", "Part sun"],
          watering: "Average",
          indoor: true,
          poisonous_to_humans: false,
          poisonous_to_pets: false,
          drought_tolerant: false,
          soil: ["Well-drained", "Loamy"],
          notes: "Produces baby plants (spiderettes) that can be propagated. Safe for pets!",
          ownerId: userId,
        },
        {
          common_name: "Peace Lily",
          scientific_name: ["Spathiphyllum wallisii"],
          family: "Araceae",
          type: "Perennial",
          care_level: "Low",
          sunlight: ["Low", "Part shade"],
          watering: "Average",
          indoor: true,
          poisonous_to_humans: true,
          poisonous_to_pets: true,
          drought_tolerant: false,
          soil: ["Well-drained", "Loamy", "Peat moss"],
          notes: "Beautiful white flowers. Good air purifier. Wilts when it needs water.",
          ownerId: userId,
        },
      ];

      // Add all sample plants
      const batch = db.batch();
      const plantRefs = samplePlants.map((plant) =>
        db.collection("users").doc(userId).collection("plants").doc()
      );

      plantRefs.forEach((ref, index) => {
        batch.set(ref, samplePlants[index]);
      });

      await batch.commit();

      return {
        message: "Sample plants initialized successfully!",
        plantsAdded: samplePlants.length,
      };
    } catch (error: unknown) {
      console.error("Error initializing sample plants:", error);

      const message =
        error instanceof Error ? error.message : "Unknown error";

      throw new HttpsError(
        "internal",
        `Error initializing sample plants: ${message}`
      );
    }
  }
);