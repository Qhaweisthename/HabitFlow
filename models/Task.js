const mongoose = require('mongoose');

const TaskSchema = new mongoose.Schema({
  name: { type: String, required: true },
  isDone: { type: Boolean, default: false },
  date: { type: String, required: true },
});

module.exports = mongoose.model('Task', TaskSchema);
