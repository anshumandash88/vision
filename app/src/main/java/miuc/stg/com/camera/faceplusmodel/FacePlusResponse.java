package miuc.stg.com.camera.faceplusmodel;

import java.util.List;

/**
 * Created by Anshuman on 27-01-2016.
 */
public class FacePlusResponse {
    public List<Face> face;

    public class Face {
        public Attribute attribute;
    }

    public class Attribute {
        public Age age;
        public Gender gender;
        public Glass glass;
        public Smiling smiling;
    }

    public class Age{
        public Integer range;
        public Integer value;
    }

    public class Gender{
        public Double confidence;
        public String value;
    }

    public class Glass {

        public Double confidence;
        public String value;
    }

    public class Smiling {
        public Double value;

    }

    public String toString(){
        int count = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("We found ");
        if(face == null && face.size() <= 0)
            return sb.append("no faces in the picture.").toString();
        else
        {
            sb.append(face.size());
            sb.append(" face.");
            for (Face myFace :face
                 ) {
                sb.append(" Face ");
                sb.append(++count);
                sb.append(".  Age ");
                sb.append(myFace.attribute.age.value);
                sb.append(".Gender ");
                sb.append(myFace.attribute.gender.value);
                sb.append(".Glasses ");
                sb.append(myFace.attribute.glass.value);
                sb.append(".Smile ");
                if(myFace.attribute.smiling.value > 5.0)
                    sb.append("Yes.   ");
                else
                    sb.append("No.    ");
            }
        }
        return sb.toString();
    }

}