
const UserModel = require("../models/UserModel");

const safeUser = (user) => ({
    id: user.id,
    name: user.name,
    email: user.email,
    phone: user.phone,
    roleId: user.roleId,
    status: user.status,
    createdAt: user.createdAt
});

const createUser = async (req, res) => {
    try {
        const { name, email, phone, password } = req.body;

        if (
            typeof name !== "string" ||
            typeof email !== "string" ||
            typeof password !== "string" ||
            !name.trim() ||
            !email.trim() ||
            !password
        ) {
            return res.status(400).json({
                message: "Name, email and password are required."
            });
        }

        if (name.trim().length > 150) {
            return res.status(400).json({
                message: "Name cannot exceed 150 characters."
            });
        }

        if (email.trim().length > 150) {
            return res.status(400).json({
                message: "Email cannot exceed 150 characters."
            });
        }

        if (phone != null && String(phone).length > 30) {
            return res.status(400).json({
                message: "Phone number cannot exceed 30 characters."
            });
        }

        const normalizedEmail = email.trim().toLowerCase();
        const existingUser = await UserModel.getByEmail(normalizedEmail);

        if (existingUser) {
            return res.status(409).json({
                message: "A user with this email already exists."
            });
        }

        // Public registration can only create a standard User.
        const user = await UserModel.create({
            name: name.trim(),
            email: normalizedEmail,
            phone,
            password,
            roleId: 1
        });

        return res.status(201).json({
            message: "User registered successfully.",
            user: safeUser(user)
        });
    } catch (error) {
        if (error.number === 2601 || error.number === 2627) {
            return res.status(409).json({
                message: "A user with this email already exists."
            });
        }

        console.error("Error creating user:", error.message);

        return res.status(500).json({
            message: "Error creating user."
        });
    }
};

const getUsers = async (req, res) => {
    try {
        const users = await UserModel.getAll();

        return res.status(200).json({
            message: "Users retrieved successfully.",
            users: users.map(safeUser)
        });
    } catch (error) {
        console.error("Error retrieving users:", error.message);

        return res.status(500).json({
            message: "Error retrieving users."
        });
    }
};

const getUserById = async (req, res) => {
    try {
        const id = Number(req.params.id);

        if (!Number.isInteger(id) || id < 1) {
            return res.status(400).json({
                message: "A valid user ID is required."
            });
        }

        const user = await UserModel.getById(id);

        if (!user) {
            return res.status(404).json({
                message: "User not found."
            });
        }

        // Users can view their own profile; admins can view any profile.
        if (
            Number(req.user.id) !== user.id &&
            Number(req.user.roleId) !== 2
        ) {
            return res.status(403).json({
                message: "You cannot access this user's profile."
            });
        }

        return res.status(200).json({
            user: safeUser(user)
        });
    } catch (error) {
        console.error("Error retrieving user:", error.message);

        return res.status(500).json({
            message: "Error retrieving user."
        });
    }
};

module.exports = { createUser, getUsers, getUserById };
