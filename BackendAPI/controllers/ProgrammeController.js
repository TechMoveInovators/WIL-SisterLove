const ProgrammeModel = require("../models/ProgrammeModel");

// Create programme
const createProgramme = (req, res) => {

    const {
        title,
        description,
        category
    } = req.body;

    // Validate required fields
    if (!title || !description || !category) {
        return res.status(400).json({
            message: "Title, description and category are required"
        });
    }

    const programme = ProgrammeModel.create({
        title,
        description,
        category
    });

    res.status(201).json({
        message: "Programme created successfully",
        programme: programme
    });
};


// Get all programmes
const getProgrammes = (req, res) => {

    const programmes = ProgrammeModel.getAll();

    res.status(200).json({
        message: "Programmes retrieved successfully",
        programmes: programmes
    });
};


// Get one programme
const getProgrammeById = (req, res) => {

    const programme = ProgrammeModel.getById(req.params.id);

    if (!programme) {
        return res.status(404).json({
            message: "Programme not found"
        });
    }

    res.status(200).json({
        programme: programme
    });
};


module.exports = {
    createProgramme,
    getProgrammes,
    getProgrammeById
};