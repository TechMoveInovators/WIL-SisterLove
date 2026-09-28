const express = require("express");

const {
    getRoles,
    getRoleById,
    createRole
} = require("../controllers/RoleController");

const router = express.Router();

router.get("/", getRoles);
router.get("/:id", getRoleById);
router.post("/", createRole);

module.exports = router;
