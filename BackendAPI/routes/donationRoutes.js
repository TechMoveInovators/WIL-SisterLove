const express = require("express");
const router = express.Router();

const {
    createDonation,
    getDonations,
    getDonationById
} = require("../controllers/DonationController");



router.post("/", createDonation);
router.get("/", getDonations);
router.get("/:id", getDonationById);

module.exports = router;


router.post("/", createDonation);
router.get("/", getDonations);
router.get("/:id", getDonationById);

module.exports = router;