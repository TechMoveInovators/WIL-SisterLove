const express = require("express");

const {
    createLegacyProject,
    getLegacyProjects,
    getLegacyProjectById
} = require("../controllers/LegacyProjectController");

const router = express.Router();

// POST - Create legacy project
router.post("/", createLegacyProject);

// GET - Get all legacy projects
router.get("/", getLegacyProjects);

// GET - Get one legacy project
router.get("/:id", getLegacyProjectById);

module.exports = router;
