/* eslint-disable */

import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
admin.initializeApp(); // Ensure this is initialized once at the top of your index.ts

const db = admin.firestore(); // Get a reference to Firestore

// Define the Plant model interface for type safety
interface Plant {
  id?: int;
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
  ownerId: string; // Crucial for linking to users
}

// Helper function to check authentication and get UID
function assertAuthenticated(request: functions.https.CallableRequest): string {
  if (!request.auth) {
    throw new functions.https.HttpsError("unauthenticated",
        "The function must be called while authenticated.");
  }
  return request.auth.uid;
}

// --- 1. Create a Plant (Callable Function) ---
export const createPlant = functions.https.onCall(async (request) => {
  const userId = assertAuthenticated(request);
  const data = request.data as Omit<Plant, 'id' | 'ownerId'>;

  // Basic validation for required fields
  if (!data.common_name || !data.scientific_name) {
    throw new functions.https.HttpsError("invalid-argument",
        "Missing required plant fields: common_name and scientific_name.");
  }

  const plantDataToSave: Plant = {
    ...data,
    ownerId: userId // Link the plant to the authenticated user
  };

  try {
    const docRef = await db.collection("plants").add(plantDataToSave);
    return {id: docRef.id, message: "Plant created successfully!"};
  } catch (error: any) {
    console.error("Error creating plant:", error);
    throw new functions.https.HttpsError("internal", `Error creating plant: ${error.message}`);
  }
});

// --- 2. Read Plants (Callable Function) ---
// This function can fetch all plants owned by the user, or a specific plant by ID
export const getPlants = functions.https.onCall(async (request) => {
  const userId = assertAuthenticated(request);
  const data = request.data as {id?: string};

  try {
    if (data.id) {
      // Get a single plant by ID, and ensure it belongs to the user
      const plantDoc = await db.collection("plants").doc(data.id).get();

      if (!plantDoc.exists) {
        throw new functions.https.HttpsError("not-found", "Plant not found.");
      }
      const plant = plantDoc.data() as Plant;

      if (plant.ownerId !== userId) {
        throw new functions.https.HttpsError('permission-denied',
            'You do not have permission to access this plant.');
      }

      return {id: plantDoc.id, ...plant};
    } else {
      // Get all plants owned by the user
      const snapshot = await db.collection("plants")
                                .where("ownerId", "==", userId)
                                .get();
      const plants: Plant[] = [];
      snapshot.forEach(doc => {
        plants.push({id: doc.id, ...doc.data() as Plant});
      });
      return plants;
    }
  } catch (error: any) {
    console.error("Error fetching plants:", error);
    // Re-throw HttpsError directly, or wrap other errors
    if (error.code && error.details) throw error;
    throw new functions.https.HttpsError("internal", `Error fetching plants: ${error.message}`);
  }
});

// --- 3. Update a Plant (Callable Function) ---
export const updatePlant = functions.https.onCall(async (request) => {
  const userId = assertAuthenticated(request);
  const data = request.data as { id: string, updates: Partial<Omit<Plant, 'id' | 'ownerId'>> };
  const plantId = data.id;
  const updates = data.updates;

  if (!plantId) {
    throw new functions.https.HttpsError("invalid-argument", "Plant ID is required.");
  }
  if (!updates || Object.keys(updates).length === 0) {
    throw new functions.https.HttpsError("invalid-argument", "No update data provided.");
  }
  // Prevent users from trying to change the ownerId directly
  if ('ownerId' in updates) {
      throw new functions.https.HttpsError("permission-denied", "Cannot change plant owner.");
  }

  try {
    const plantRef = db.collection("plants").doc(plantId);
    const plantDoc = await plantRef.get();

    if (!plantDoc.exists) {
      throw new functions.https.HttpsError("not-found", "Plant not found.");
    }
    const plant = plantDoc.data() as Plant;

    if (plant.ownerId !== userId) {
      throw new functions.https.HttpsError("permission-denied",
          "You do not have permission to update this plant.");
    }

    await plantRef.update(updates);
    return { message: "Plant updated successfully!" };
  } catch (error: any) {
    console.error("Error updating plant:", error);
    if (error.code && error.details) throw error;
    throw new functions.https.HttpsError("internal", `Error updating plant: ${error.message}`);
  }
});

// --- 4. Delete a Plant (Callable Function) ---
export const deletePlant = functions.https.onCall(async (request) => {
  const userId = assertAuthenticated(request);
  const data = request.data as {id: string};
  const plantId = data.id;

  if (!plantId) {
    throw new functions.https.HttpsError("invalid-argument", "Plant ID is required.");
  }

  try {
    const plantRef = db.collection("plants").doc(plantId);
    const plantDoc = await plantRef.get();

    if (!plantDoc.exists) {
      throw new functions.https.HttpsError("not-found", "Plant not found.");
    }
    const plant = plantDoc.data() as Plant;

    if (plant.ownerId !== userId) {
      throw new functions.https.HttpsError("permission-denied",
          "You do not have permission to delete this plant.");
    }

    await plantRef.delete();
    return { message: "Plant deleted successfully!" };
  } catch (error: any) {
    console.error("Error deleting plant:", error);
    if (error.code && error.details) throw error;
    throw new functions.https.HttpsError("internal", `Error deleting plant: ${error.message}`);
  }
});