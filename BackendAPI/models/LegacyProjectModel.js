let legacyProjects = [];

const LegacyProjectModel = {

    // Get all legacy projects
    getAll: () => {
        return legacyProjects;
    },

    // Get legacy project by ID
    getById: (id) => {
        return legacyProjects.find(
            project => project.id === Number(id)
        );
    },

    // Create legacy project
    create: (projectData) => {

        const newProject = {
            id: legacyProjects.length + 1,
            title: projectData.title,
            description: projectData.description,
            completionDate: projectData.completionDate,
            media: projectData.media || [],
            createdAt: new Date()
        };

        legacyProjects.push(newProject);

        return newProject;
    }

};

module.exports = LegacyProjectModel;
