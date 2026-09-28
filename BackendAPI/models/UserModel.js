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

    create: (userData) => {
        const newUser = {
            id: users.length + 1,
            name: userData.name,
            email: userData.email,
            phone: userData.phone || "",
            role: userData.role || "User",
            status: "Active",
            createdAt: new Date()
        };

        users.push(newUser);

        return newUser;
    }
};

module.exports = UserModel;
