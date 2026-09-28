let programmes = [];

const ProgrammeModel = {

    // Get all programmes
    getAll: () => {
        return programmes;
    },

    // Get programme by ID
    getById: (id) => {
        return programmes.find(
            programme => programme.id === Number(id)
        );
    },

    // Create programme
    create: (programmeData) => {

        const newProgramme = {
            id: programmes.length + 1,
            title: programmeData.title,
            description: programmeData.description,
            category: programmeData.category,
            status: "Active",
            createdAt: new Date()
        };

        programmes.push(newProgramme);

        return newProgramme;
    }

};

module.exports = ProgrammeModel;