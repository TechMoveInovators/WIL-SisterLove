const VolunteerModel = require("../models/VolunteerModel");

// Create volunteer application
const createVolunteer = (req, res) => {

    const {
        name,
        email,
        phone,
        areaOfInterest,
        motivation
    } = req.body;

    // Validation
    if (
        !name ||
        !email ||
        !phone ||
        !areaOfInterest ||
        !motivation
    ) {
        return res.status(400).json({
            message: "Please provide all required fields"
        });
    }

    const volunteer = VolunteerModel.create({
        name,
        email,
        phone,
        areaOfInterest,
        motivation
    });

    res.status(201).json({
        message: "Volunteer application submitted successfully",
        volunteer: volunteer
    });
};


// Get all volunteers
const getVolunteers = (req, res) => {

    const volunteers = VolunteerModel.getAll();

    res.status(200).json({
        message: "Volunteer applications retrieved successfully",
        volunteers: volunteers
    });
};


// Get volunteer by ID
const getVolunteerById = (req, res) => {

    const volunteer = VolunteerModel.getById(req.params.id);

    if (!volunteer) {
        return res.status(404).json({
            message: "Volunteer application not found"
        });
    }

    res.status(200).json({
        volunteer: volunteer
    });
};


module.exports = {
    createVolunteer,
    getVolunteers,
    getVolunteerById
};