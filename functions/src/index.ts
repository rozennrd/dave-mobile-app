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
