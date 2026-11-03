
const express = require("express");
const router = express.Router();

const {
    getRoles,
    getRoleById,
    createRole
} = require("../controllers/RoleController");

const authenticateToken = require("../middleware/authMiddleware");
const requireAdmin = require("../middleware/adminMiddleware");

// Role information can be read publicly.
router.get("/", getRoles);
router.get("/:id", getRoleById);

// Only administrators can create roles.
router.post("/", authenticateToken, requireAdmin, createRole);

module.exports = router;
