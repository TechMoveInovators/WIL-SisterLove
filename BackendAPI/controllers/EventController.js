const EventModel = require("../models/EventModel");

const createEvent = (req, res) => {
    const {
        title,
        description,
        date,
        location
    } = req.body;

    if (!title || !description || !date || !location) {
        return res.status(400).json({
            message: "Title, description, date and location are required"
        });
    }

    const event = EventModel.create({
        title,
        description,
        date,
        location
    });

    res.status(201).json({
        message: "Event created successfully",
        event: event
    });
};

const getEvents = (req, res) => {
    const events = EventModel.getAll();

    res.status(200).json({
        message: "Events retrieved successfully",
        events: events
    });
};

const getEventById = (req, res) => {
    const event = EventModel.getById(req.params.id);

    if (!event) {
        return res.status(404).json({
            message: "Event not found"
        });
    }

    res.status(200).json({
        event: event
    });
};

module.exports = {
    createEvent,
    getEvents,
    getEventById
};
