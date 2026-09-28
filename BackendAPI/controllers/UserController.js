const UserModel = require("../models/UserModel");

const createUser = (req, res) => {
    const {
        name,
        email,
        phone,
        role
    } = req.body;

    if (!name || !email) {
        return res.status(400).json({
            message: "Name and email are required"
        });
    }

    const user = UserModel.create({
        name,
        email,
        phone,
        role
    });

    res.status(201).json({
        message: "User created successfully",
        user: user
    });
};

const getUsers = (req, res) => {
    const users = UserModel.getAll();

    res.status(200).json({
        message: "Users retrieved successfully",
        users: users
    });
};

const getUserById = (req, res) => {
    const user = UserModel.getById(req.params.id);

    if (!user) {
        return res.status(404).json({
            message: "User not found"
        });
    }

    res.status(200).json({
        user: user
    });
};

module.exports = {
    createUser,
    getUsers,
    getUserById
};
