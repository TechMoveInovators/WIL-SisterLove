
const bcrypt = require("bcryptjs");
const { sql, getPool } = require("../config/db");

const mapUser = (row) => {
    if (!row) return null;

    return {
        id: row.id,
        name: row.name,
        email: row.email,
        phone: row.phone || "",
        password: row.password,
        roleId: row.roleId,
        status: row.status,
        createdAt: row.createdAt
    };
};

const UserModel = {
    getAll: async () => {
        const pool = await getPool();

        const result = await pool.request().query(`
            SELECT
                Id AS id,
                FullName AS name,
                Email AS email,
                PhoneNumber AS phone,
                PasswordHash AS password,
                RoleId AS roleId,
                'Active' AS status,
                MemberSince AS createdAt
            FROM dbo.Users
            ORDER BY Id
        `);

        return result.recordset.map(mapUser);
    },

    getById: async (id) => {
        const pool = await getPool();

        const result = await pool.request()
            .input("id", sql.Int, Number(id))
            .query(`
                SELECT
                    Id AS id,
                    FullName AS name,
                    Email AS email,
                    PhoneNumber AS phone,
                    PasswordHash AS password,
                    RoleId AS roleId,
                    'Active' AS status,
                    MemberSince AS createdAt
                FROM dbo.Users
                WHERE Id = @id
            `);

        return mapUser(result.recordset[0]);
    },

    getByEmail: async (email) => {
        const pool = await getPool();

        const result = await pool.request()
            .input("email", sql.NVarChar(150), email.trim())
            .query(`
                SELECT
                    Id AS id,
                    FullName AS name,
                    Email AS email,
                    PhoneNumber AS phone,
                    PasswordHash AS password,
                    RoleId AS roleId,
                    'Active' AS status,
                    MemberSince AS createdAt
                FROM dbo.Users
                WHERE Email = @email
            `);

        return mapUser(result.recordset[0]);
    },

    create: async (userData) => {
        const hashedPassword = await bcrypt.hash(
            userData.password,
            10
        );

        const pool = await getPool();

        const result = await pool.request()
            .input("name", sql.NVarChar(150), userData.name.trim())
            .input("email", sql.NVarChar(150), userData.email.trim())
            .input("phone", sql.NVarChar(30), userData.phone?.trim() || null)
            .input("roleId", sql.Int, userData.roleId || 1)
            .input("password", sql.NVarChar(255), hashedPassword)
            .query(`
                INSERT INTO dbo.Users
                    (FullName, Email, PhoneNumber, MemberSince, RoleId, PasswordHash)
                OUTPUT
                    INSERTED.Id AS id,
                    INSERTED.FullName AS name,
                    INSERTED.Email AS email,
                    INSERTED.PhoneNumber AS phone,
                    INSERTED.PasswordHash AS password,
                    INSERTED.RoleId AS roleId,
                    'Active' AS status,
                    INSERTED.MemberSince AS createdAt
                VALUES
                    (@name, @email, @phone, SYSUTCDATETIME(), @roleId, @password)
            `);

        return mapUser(result.recordset[0]);
    },

    checkPassword: async (password, hashedPassword) => {
        return bcrypt.compare(password, hashedPassword);
    }
};

module.exports = UserModel;
