import express from "express";
import Task from "../models/Task.js";

const router = express.Router();

// ✅ GET all tasks
router.get("/", async (req, res) => {
  try {
    const tasks = await Task.find();
    res.json(tasks);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// ✅ POST create new task
router.post("/", async (req, res) => {
  try {
    const { name, isDone, date } = req.body;
    const newTask = new Task({ name, isDone, date });
    const savedTask = await newTask.save();
    res.status(201).json(savedTask); // ✅ send task object back
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
});

// ✅ PUT update task
router.put("/:id", async (req, res) => {
  try {
    const updated = await Task.findByIdAndUpdate(req.params.id, req.body, { new: true });
    res.json(updated);
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
});

// ✅ DELETE task
router.delete("/:id", async (req, res) => {
  try {
    await Task.findByIdAndDelete(req.params.id);
    res.json({ message: "Task deleted" });
  } catch (err) {
    res.status(400).json({ message: err.message });
  }
});

export default router;
