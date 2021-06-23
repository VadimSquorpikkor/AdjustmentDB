package com.squorpikkor.app.adjustmentdb.ui.main.entities;
   class State extends Entity{

      String type;
      String location;

      public State(String id, String nameId, String nameRu, String type, String location) {
         super(id, nameId, nameRu);
         this.type = type;
         this.location = location;
      }

      public String getType() {
         return type;
      }

      public String getLocation() {
         return location;
      }
   }
