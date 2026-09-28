let roles = [
    {
        id: 1,
        name: "User",
        description: "Registered website user"
    },
    {
        id: 2,
        name: "Administrator",
        description: "Administrator who manages website content"
    }
];

const RoleModel = {
    getAll: () => {
        return roles;
    },

    getById: (id) => {
        return roles.find(
            role => role.id === Number(id)
        );
    },

    getByName: (name) => {
        return roles.find(
            role => role.name.toLowerCase() === name.toLowerCase()
        );
    },

    create: (roleData) => {
        const newRole = {
            id: roles.length + 1,
            name: roleData.name,
            description: roleData.description
        };

        roles.push(newRole);

        return newRole;
    }
};

module.exports = RoleModel;
