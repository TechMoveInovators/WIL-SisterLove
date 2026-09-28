const express = require("express");

const {
    createPartnership,
    getPartnerships,
    getPartnershipById
} = require("../controllers/PartnershipController");

const router = express.Router();

// POST - Create partnership
router.post("/", createPartnership);

// GET - Get all partnerships
router.get("/", getPartnerships);

// GET - Get one partnership
router.get("/:id", getPartnershipById);

module.exports = router;
