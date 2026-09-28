const express = require("express");

const {
    createVolunteer,
    getVolunteers,
    getVolunteerById
} = require("../controllers/VolunteerController");

const router = express.Router();

// POST - Create volunteer application
router.post("/", createVolunteer);

// GET - Get all volunteer applications
router.get("/", getVolunteers);

// GET - Get one volunteer application
router.get("/:id", getVolunteerById);

module.exports = router;