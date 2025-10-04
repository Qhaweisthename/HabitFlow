import express from "express";
import mongoose from "mongoose";
import cors from "cors";

const app = express();
app.use(cors());
app.use(express.json());

mongoose.connect("mongodb+srv://user_insy7314:NfETjaixGWIytqYJ@cluster0.wbvqneq.mongodb.net/habitflow", {
  useNewUrlParser: true,
  useUnifiedTopology: true,
})
  .then(() => console.log("✅ Connected to MongoDB"))
  .catch(err => console.error("❌ MongoDB connection error:", err));

const taskSchema = new mongoose.Schema({
  name: String,
  date: String,
});

const Task = mongoose.model("Task", taskSchema);

app.post("/tasks", async (req, res) => {
  try {
    const { name, date } = req.body;
    const newTask = new Task({ name, date });
    await newTask.save();
    res.status(201).json(newTask);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.get("/tasks", async (req, res) => {
  try {
    const tasks = await Task.find();
    res.json(tasks);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.listen(5000, () => console.log("✅ Server running on http://localhost:5000"));
