package com.example.billmac1.gridview4;

/**
 * Created by billmac1 on 1/8/2016.
 */
public class GridItem {
    private String image;
    private String title;
    private String overview;
    private String rating;
    private String release_date;

    public GridItem() {
        super();
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {    return title;    }
    public void setTitle(String title) { this.title = title;   }

    public String getOverview() {    return overview;    }
    public void setOverview(String overview) { this.overview = overview;   }

    public String getRating() {    return rating;    }
    public void setRating(String rating) { this.rating = rating;   }

    public String getReleaseDate() {    return release_date;    }
    public void setReleaseDate(String release_date) { this.release_date = release_date;   }



}