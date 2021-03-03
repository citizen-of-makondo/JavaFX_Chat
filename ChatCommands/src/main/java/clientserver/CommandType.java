package clientserver;

public enum CommandType {
    AUTH,
    AUTH_OK,
    AUTH_ERROR,
    PRIVATE_MESSAGE,
    PUBLIC_MESSAGE,
    INFO_MESSAGE,
    ERROR,
    END,
    UPDATE_USERS_LIST,
    REG,
    REG_OK,
    REG_ERROR,
    CHANGE_NAME,
    CHANGENAME_ERROR,
    CHANGENAME_OK,
}