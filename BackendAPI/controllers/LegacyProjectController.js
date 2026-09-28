const LegacyProjectModel = require("../models/LegacyProjectModel");

// Create legacy project
const createLegacyProject = (req, res) => {

    const {
        title,
        description,
        completionDate,
        media
    } = req.body;

    // Validate required fields
    if (!title || !description || !completionDate) {
        return res.status(400).json({
            message: "Title, description and completion date are required"
        });
    }

    const project = LegacyProjectModel.create({
        title,
        description,
        completionDate,
        media
    });

    res.status(201).json({
        message: "Legacy project created successfully",
        project: project
    });
};


// Get all legacy projects
const getLegacyProjects = (req, res) => {

    const projects = LegacyProjectModel.getAll();

    res.status(200).json({
        message: "Legacy projects retrieved successfully",
        projects: projects
    });
};


// Get one legacy project
const getLegacyProjectById = (req, res) => {

    const project = LegacyProjectModel.getById(req.params.id);

    if (!project) {
        return res.status(404).json({
            message: "Legacy project not found"
        });
    }

    res.status(200).json({
        project: project
    });
};


module.exports = {
    createLegacyProject,
    getLegacyProjects,
    getLegacyProjectById
};
