const RoleModel = require("../models/RoleModel");

const getRoles = (req, res) => {
    const roles = RoleModel.getAll();

    res.status(200).json({
        message: "Roles retrieved successfully",
        roles: roles
    });
};

const getRoleById = (req, res) => {
    const role = RoleModel.getById(req.params.id);

    if (!role) {
        return res.status(404).json({
            message: "Role not found"
        });
    }

    res.status(200).json({
        role: role
    });
};

const createRole = (req, res) => {
    const {
        name,
        description
    } = req.body;

    if (!name || !description) {
        return res.status(400).json({
            message: "Name and description are required"
        });
    }

    const existingRole = RoleModel.getByName(name);

    if (existingRole) {
        return res.status(400).json({
            message: "Role already exists"
        });
    }

    const role = RoleModel.create({
        name,
        description
    });

    res.status(201).json({
        message: "Role created successfully",
        role: role
    });
};

module.exports = {
    getRoles,
    getRoleById,
    createRole
};
