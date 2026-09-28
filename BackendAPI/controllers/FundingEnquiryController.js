const FundingEnquiryModel = require("../models/FundingEnquiryModel");

// Create funding enquiry
const createFundingEnquiry = (req, res) => {

    const {
        name,
        email,
        organisation,
        message
    } = req.body;

    // Validate required fields
    if (!name || !email || !organisation || !message) {
        return res.status(400).json({
            message: "Name, email, organisation and message are required"
        });
    }

    const enquiry = FundingEnquiryModel.create({
        name,
        email,
        organisation,
        message
    });

    res.status(201).json({
        message: "Funding enquiry submitted successfully",
        enquiry: enquiry
    });
};


// Get all funding enquiries
const getFundingEnquiries = (req, res) => {

    const enquiries = FundingEnquiryModel.getAll();

    res.status(200).json({
        message: "Funding enquiries retrieved successfully",
        enquiries: enquiries
    });
};


// Get one funding enquiry
const getFundingEnquiryById = (req, res) => {

    const enquiry = FundingEnquiryModel.getById(req.params.id);

    if (!enquiry) {
        return res.status(404).json({
            message: "Funding enquiry not found"
        });
    }

    res.status(200).json({
        enquiry: enquiry
    });
};


module.exports = {
    createFundingEnquiry,
    getFundingEnquiries,
    getFundingEnquiryById
};