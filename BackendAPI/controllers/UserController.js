const UserModel = require("../models/UserModel");

const createUser = async (req, res) => {
    try {
        const {
            name,
            email,
            phone,
            password,
            roleId
        } = req.body;

        if (!name || !email || !password) {
            return res.status(400).json({
                message: "Name, email and password are required"
            });
        }

        const existingUser = UserModel.getByEmail(email);

        if (existingUser) {
            return res.status(400).json({
                message: "A user with this email already exists"
            });
        }

        const user = await UserModel.create({
            name,
            email,
            phone,
            password,
            roleId
        });

        const safeUser = {
            id: user.id,
            name: user.name,
            email: user.email,
            phone: user.phone,
            roleId: user.roleId,
            status: user.status,
            createdAt: user.createdAt
        };

        res.status(201).json({
            message: "User registered successfully",
            user: safeUser
        });

    } catch (error) {
        res.status(500).json({
            message: "Error creating user",
            error: error.message
        });
    }
};

const getUsers = (req, res) => {
    const users = UserModel.getAll();

    const safeUsers = users.map(user => ({
        id: user.id,
        name: user.name,
        email: user.email,
        phone: user.phone,
        roleId: user.roleId,
        status: user.status,
        createdAt: user.createdAt
    }));

    res.status(200).json({
        message: "Users retrieved successfully",
        users: safeUsers
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
        user: {
            id: user.id,
            name: user.name,
            email: user.email,
            phone: user.phone,
            roleId: user.roleId,
            status: user.status,
            createdAt: user.createdAt
        }
    });
};

module.exports = {
    createUser,
    getUsers,
    getUserById
};
