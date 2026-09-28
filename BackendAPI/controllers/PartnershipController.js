const PartnershipModel = require("../models/PartnershipModel");

// Create partnership
const createPartnership = (req, res) => {

    const {
        name,
        description,
        logo,
        website
    } = req.body;

    // Validate required fields
    if (!name || !description) {
        return res.status(400).json({
            message: "Name and description are required"
        });
    }

    const partnership = PartnershipModel.create({
        name,
        description,
        logo,
        website
    });

    res.status(201).json({
        message: "Partnership created successfully",
        partnership: partnership
    });
};


// Get all partnerships
const getPartnerships = (req, res) => {

    const partnerships = PartnershipModel.getAll();

    res.status(200).json({
        message: "Partnerships retrieved successfully",
        partnerships: partnerships
    });
};


// Get one partnership
const getPartnershipById = (req, res) => {

    const partnership = PartnershipModel.getById(req.params.id);

    if (!partnership) {
        return res.status(404).json({
            message: "Partnership not found"
        });
    }

    res.status(200).json({
        partnership: partnership
    });
};


module.exports = {
    createPartnership,
    getPartnerships,
    getPartnershipById
};
