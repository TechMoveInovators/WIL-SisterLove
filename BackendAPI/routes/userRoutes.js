
const express = require("express");
const router = express.Router();

const {
    createUser,
    getUsers,
    getUserById
} = require("../controllers/UserController");

const authenticateToken = require("../middleware/authMiddleware");
const requireAdmin = require("../middleware/adminMiddleware");

// Public registration; role is always assigned server-side.
router.post("/", createUser);

// Only administrators can list all users.
router.get("/", authenticateToken, requireAdmin, getUsers);

// Authenticated users can access their own profile.
// Administrators can access any user's profile.
router.get("/:id", authenticateToken, getUserById);

module.exports = router;
