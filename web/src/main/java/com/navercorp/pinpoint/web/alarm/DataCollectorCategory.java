package com.navercorp.pinpoint.web.alarm;

public enum DataCollectorCategory {
    RESPONSE_TIME {
        @Override
        public boolean isRequireAgentList() {
            return false;
        }
    },
    AGENT_STAT {
        @Override
        public boolean isRequireAgentList() {
            return true;
        }
    },
    AGENT_EVENT {
        @Override
        public boolean isRequireAgentList() {
            return true;
        }
    },
    DATA_SOURCE_STAT {
        @Override
        public boolean isRequireAgentList() {
            return true;
        }
    },
    CALLER_STAT {
        @Override
        public boolean isRequireAgentList() {
            return false;
        }
    },
    FILE_DESCRIPTOR {
        @Override
        public boolean isRequireAgentList() {
            return true;
        }
    };

    public abstract boolean isRequireAgentList();
}