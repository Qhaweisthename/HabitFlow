# Use an official Node runtime
FROM node:18

# Set working directory
WORKDIR /app

# Copy package files first (for caching)
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of your files
COPY . .

# Expose the port your app uses
EXPOSE 5000

# Start the app
CMD ["node", "server.js"]
