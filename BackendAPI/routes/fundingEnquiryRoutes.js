const express = require("express");

const {
    createFundingEnquiry,
    getFundingEnquiries,
    getFundingEnquiryById
} = require("../controllers/FundingEnquiryController");

const router = express.Router();

// POST - Submit funding enquiry
router.post("/", createFundingEnquiry);

// GET - Get all funding enquiries
router.get("/", getFundingEnquiries);

// GET - Get one funding enquiry
router.get("/:id", getFundingEnquiryById);

module.exports = router;
