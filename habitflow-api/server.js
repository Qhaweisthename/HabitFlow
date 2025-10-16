import express from "express";
import mongoose from "mongoose";
import cors from "cors";
import dotenv from "dotenv";

dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

// ✅ Connect to MongoDB
mongoose
  .connect(
    "mongodb+srv://user_insy7314:btbwvHClveR3f5pw@cluster0.wbvqneq.mongodb.net/habitflow"
  )
  .then(() => console.log("✅ Connected to MongoDB"))
  .catch((err) => console.error("❌ MongoDB connection error:", err));

// ✅ Define Mongoose schema and model
const taskSchema = new mongoose.Schema({
  name: { type: String, required: true },
  isDone: { type: Boolean, default: false },
  date: { type: String, required: true },
});

const Task = mongoose.model("Task", taskSchema);

// ✅ Routes

// Get all tasks
app.get("/api/tasks", async (req, res) => {
  try {
    const tasks = await Task.find();
    res.json(tasks);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Add new task
app.post("/api/tasks", async (req, res) => {
  try {
    const { name, isDone, date } = req.body;
    const newTask = new Task({ name, isDone, date });
    const saved = await newTask.save();
    res.status(201).json(saved);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Update task
app.put("/api/tasks/:id", async (req, res) => {
  try {
    const updated = await Task.findByIdAndUpdate(req.params.id, req.body, {
      new: true,
    });
    res.json(updated);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Delete task
app.delete("/api/tasks/:id", async (req, res) => {
  try {
    await Task.findByIdAndDelete(req.params.id);
    res.json({ message: "Task deleted" });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Root route
app.get("/", (req, res) => {
  res.send("✅ HabitFlow API is running...");
});

// ✅ Start server
const PORT = process.env.PORT || 5000;
app.listen(PORT, () =>
  console.log(`🚀 Server running on http://localhost:${PORT}`)
);
