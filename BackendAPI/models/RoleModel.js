
const { sql, getPool } = require("../config/db");

const RoleModel = {
    getAll: async () => {
        const pool = await getPool();

        const result = await pool.request().query(`
            SELECT
                Id AS id,
                RoleName AS name,
                Description AS description
            FROM dbo.Roles
            ORDER BY Id
        `);

        return result.recordset;
    },

    getById: async (id) => {
        const pool = await getPool();

        const result = await pool.request()
            .input("id", sql.Int, Number(id))
            .query(`
                SELECT
                    Id AS id,
                    RoleName AS name,
                    Description AS description
                FROM dbo.Roles
                WHERE Id = @id
            `);

        return result.recordset[0] || null;
    },

    getByName: async (name) => {
        const pool = await getPool();

        const result = await pool.request()
            .input("name", sql.NVarChar(50), name.trim())
            .query(`
                SELECT
                    Id AS id,
                    RoleName AS name,
                    Description AS description
                FROM dbo.Roles
                WHERE RoleName = @name
            `);

        return result.recordset[0] || null;
    },

    create: async (roleData) => {
        const pool = await getPool();

        const result = await pool.request()
            .input("name", sql.NVarChar(50), roleData.name.trim())
            .input(
                "description",
                sql.NVarChar(250),
                roleData.description || null
            )
            .query(`
                INSERT INTO dbo.Roles (RoleName, Description)
                OUTPUT
                    INSERTED.Id AS id,
                    INSERTED.RoleName AS name,
                    INSERTED.Description AS description
                VALUES (@name, @description)
            `);

        return result.recordset[0];
    }
};

module.exports = RoleModel;
