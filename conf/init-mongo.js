db.createUser(
    {
        user    : "rootuser",
        pwd     : "rootpass",
        roles   : ["root"]
    }
)
db.createUser(
    {
        user    : "streaming-api-user",
        pwd     : "streaming-api-password",
        roles   : [
            {
                role: "readWrite",
                db  : "streaming-api-backend"
            }
        ]
    }
)