package com.github.a28hacks.driveby.model.database;


import io.realm.RealmObject;

public class InfoChunk extends RealmObject {

    private String sentence;
    private boolean wasTold;

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public boolean wasTold() {
        return wasTold;
    }

    public void setTold(boolean wasTold) {
        this.wasTold = wasTold;
    }
}
