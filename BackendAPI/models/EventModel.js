let events = [];

const EventModel = {
    getAll: () => {
        return events;
    },

    getById: (id) => {
        return events.find(
            event => event.id === Number(id)
        );
    },

    create: (eventData) => {
        const newEvent = {
            id: events.length + 1,
            title: eventData.title,
            description: eventData.description,
            date: eventData.date,
            location: eventData.location,
            status: "Upcoming",
            createdAt: new Date()
        };

        events.push(newEvent);

        return newEvent;
    }
};

module.exports = EventModel;
