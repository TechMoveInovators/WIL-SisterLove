const express = require("express");

const {
    createProgramme,
    getProgrammes,
    getProgrammeById
} = require("../controllers/ProgrammeController");

const router = express.Router();

// POST - Create programme
router.post("/", createProgramme);

// GET - Get all programmes
router.get("/", getProgrammes);

// GET - Get one programme
router.get("/:id", getProgrammeById);

module.exports = router;