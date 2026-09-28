const bcrypt = require("bcryptjs");

let users = [];

const UserModel = {
    getAll: () => {
        return users;
    },

    getById: (id) => {
        return users.find(
            user => user.id === Number(id)
        );
    },

    getByEmail: (email) => {
        return users.find(
            user => user.email.toLowerCase() === email.toLowerCase()
        );
    },

    create: async (userData) => {
        const hashedPassword = await bcrypt.hash(
            userData.password,
            10
        );

        const newUser = {
            id: users.length + 1,
            name: userData.name,
            email: userData.email,
            phone: userData.phone || "",
            password: hashedPassword,
            roleId: userData.roleId || 1,
            status: "Active",
            createdAt: new Date()
        };

        users.push(newUser);

        return newUser;
    },

    checkPassword: async (password, hashedPassword) => {
        return await bcrypt.compare(
            password,
            hashedPassword
        );
    }
};

module.exports = UserModel;
