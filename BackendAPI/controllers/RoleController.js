
const RoleModel = require("../models/RoleModel");

const getRoles = async (req, res) => {
    try {
        const roles = await RoleModel.getAll();

        return res.status(200).json({
            message: "Roles retrieved successfully",
            roles
        });
    } catch (error) {
        console.error("Error retrieving roles:", error.message);

        return res.status(500).json({
            message: "Error retrieving roles"
        });
    }
};

const getRoleById = async (req, res) => {
    try {
        const role = await RoleModel.getById(req.params.id);

        if (!role) {
            return res.status(404).json({
                message: "Role not found"
            });
        }

        return res.status(200).json({ role });
    } catch (error) {
        console.error("Error retrieving role:", error.message);

        return res.status(500).json({
            message: "Error retrieving role"
        });
    }
};

const createRole = async (req, res) => {
    try {
        const { name, description } = req.body;

        if (!name || !name.trim()) {
            return res.status(400).json({
                message: "Role name is required"
            });
        }

        if (name.trim().length > 50) {
            return res.status(400).json({
                message: "Role name cannot exceed 50 characters"
            });
        }

        if (description && description.length > 250) {
            return res.status(400).json({
                message: "Description cannot exceed 250 characters"
            });
        }

        const existingRole = await RoleModel.getByName(name);

        if (existingRole) {
            return res.status(409).json({
                message: "Role already exists"
            });
        }

        const role = await RoleModel.create({
            name,
            description
        });

        return res.status(201).json({
            message: "Role created successfully",
            role
        });
    } catch (error) {
        if (error.number === 2601 || error.number === 2627) {
            return res.status(409).json({
                message: "Role already exists"
            });
        }

        console.error("Error creating role:", error.message);

        return res.status(500).json({
            message: "Error creating role"
        });
    }
};

module.exports = { getRoles, getRoleById, createRole };
