const express = require("express");

const {
    createEvent,
    getEvents,
    getEventById
} = require("../controllers/EventController");

const router = express.Router();

router.post("/", createEvent);
router.get("/", getEvents);
router.get("/:id", getEventById);

module.exports = router;