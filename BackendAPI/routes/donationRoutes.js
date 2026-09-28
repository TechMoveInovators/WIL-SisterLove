const express = require("express");

const {
    createDonation,
    getDonations,
    getDonationById
} = require("../controllers/DonationController");

const router = express.Router();

router.post("/", createDonation);
router.get("/", getDonations);
router.get("/:id", getDonationById);

module.exports = router;
const express = require("express");

const {
    createDonation,
    getDonations,
    getDonationById
} = require("../controllers/DonationController");

const router = express.Router();

router.post("/", createDonation);
router.get("/", getDonations);
router.get("/:id", getDonationById);

module.exports = router;