package com.android.blendershape3.util;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.android.blendershape3.util.Constants.BYTES_PER_FLOAT;


public class ShapeHelper {

    private static final String TAG ="ShaderHelper" ;
    private List<String> outputVertexList;
    private List<String> outputNormalList;
    private List<String> outputFacesList;

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalsBuffer;

    /**
     * @param vertexList
     * @param normalsList
     * @param facesList
     * @param positionComponentCount
     * @param normalComponentCount
     * @return
     */
    public FloatBuffer[] getFacesNormalsBuffers(List<String> vertexList, List<String> normalsList, List<String> facesList,
                                                int positionComponentCount, int normalComponentCount) {
        FloatBuffer[] resultBuffers = new FloatBuffer[2];

        outputVertexList = new ArrayList<>();
        outputNormalList = new ArrayList<>();
        outputFacesList = new ArrayList<>();

        for (String faceAndNormal : facesList) {
            String combinedVI[] = faceAndNormal.split(" "); //combinedVI[0]="f"

            //są 4 elementy: f 5//1 3//1 1//1
            String complet1[] = combinedVI[1].split("//"); //returns 2 elements: complet1[0] is face vertex index
            String complet2[] = combinedVI[2].split("//");    //and complet1[1] is normal index
            String complet3[] = combinedVI[3].split("//");

            int vertex1 = Integer.parseInt(complet1[0]);
            int normal1 = Integer.parseInt(complet1[1]);
            int vertex2 = Integer.parseInt(complet2[0]);
            int normal2 = Integer.parseInt(complet2[1]);
            int vertex3 = Integer.parseInt(complet3[0]);
            int normal3 = Integer.parseInt(complet3[1]);

            // each index is from 1, so decrease them by 1
            outputVertexList.add(vertexList.get(vertex1 - 1));
            outputVertexList.add(vertexList.get(vertex2 - 1));
            outputVertexList.add(vertexList.get(vertex3 - 1));

            outputNormalList.add(normalsList.get(normal1 - 1));
            outputNormalList.add(normalsList.get(normal2 - 1));
            outputNormalList.add(normalsList.get(normal3 - 1));
        }

        //parsing vertices lines into floats
        //Buffer containing all vertices in float coordinates
        vertexBuffer = ByteBuffer.allocateDirect(outputVertexList.size() *
                positionComponentCount * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.position(0);

        for (String line : outputVertexList) {
            String coords[] = line.split(" "); // Split by space. coords[0]="v"
            float x = Float.parseFloat(coords[1]);
            float y = Float.parseFloat(coords[2]);
            float z = Float.parseFloat(coords[3]);

            vertexBuffer.put(x);
            vertexBuffer.put(y);
            vertexBuffer.put(z);
        }
        vertexBuffer.position(0);

        //parsing normals into floats
        //This buffer will contain all normal vectors in the same order as vertices (above)
        normalsBuffer = ByteBuffer.allocateDirect(outputNormalList.size() *
                normalComponentCount * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalsBuffer.position(0);

        for (String line : outputNormalList) {
            String normals[] = line.split(" ");  //normals[0]="vn"
            float x = Float.parseFloat(normals[1]);
            float y = Float.parseFloat(normals[2]);
            float z = Float.parseFloat(normals[3]);

            normalsBuffer.put(x);
            normalsBuffer.put(y);
            normalsBuffer.put(z);
        }
        normalsBuffer.position(0);

        resultBuffers[0] = vertexBuffer;
        resultBuffers[1] = normalsBuffer;

        return resultBuffers;

    }


    public FloatBuffer[] getVertexNormalsBuffers(List<String> vertexList, List<String> normalsList, List<String> facesList,
                                                 int positionComponentCount, int normalComponentCount) {

        outputVertexList = new ArrayList<>();
        outputNormalList = new ArrayList<>();
//        outputFacesList = new ArrayList<>();
        List<String> normalsForAverage;
        List<Integer> foundVericesIndices;

        FloatBuffer[] resultBuffers = new FloatBuffer[2];

        //find all vertex instances


        for (String faceAndNormal : facesList) {
            //split line by " "
            String combinedVI[] = faceAndNormal.split(" "); //combinedVI[0]="f"

            //są 4 elementy: f 5//1 3//1 1//1
            String complet1[] = combinedVI[1].split("//"); //returns 2 elements: complet1[0] is face vertex index
            String complet2[] = combinedVI[2].split("//");    //and complet1[1] is normal index
            String complet3[] = combinedVI[3].split("//");

            int vertex1 = Integer.parseInt(complet1[0]);
            int normal1 = Integer.parseInt(complet1[1]);
            int vertex2 = Integer.parseInt(complet2[0]);
            int normal2 = Integer.parseInt(complet2[1]);
            int vertex3 = Integer.parseInt(complet3[0]);
            int normal3 = Integer.parseInt(complet3[1]);

            // each index is from 1, so decrease them by 1
            outputVertexList.add(vertexList.get(vertex1 - 1));
            outputVertexList.add(vertexList.get(vertex2 - 1));
            outputVertexList.add(vertexList.get(vertex3 - 1));

            outputNormalList.add(normalsList.get(normal1 - 1));
            outputNormalList.add(normalsList.get(normal2 - 1));
            outputNormalList.add(normalsList.get(normal3 - 1));
        }

        //po oryginalnej liście vertexów można znaleźć wszystkie wystąpienia
        for (String vertex : vertexList) {
            normalsForAverage=new ArrayList<>();
            foundVericesIndices=new ArrayList<>();

            for (int i=0;i<outputVertexList.size();i++) {
                if (outputVertexList.get(i).equals(vertex)) {
                    //dodaj normalną do uśrednienia
                    normalsForAverage.add(outputNormalList.get(i));

                    //dodaj index znalezionego Vertexu aby uśrednioną normalną wpisac do normalList w odpowiednim miejscu
                    foundVericesIndices.add(i);

                }
                //uśrednij normalne
                String averageNormal;
                //jeśli znaleziono więcej niż jedno wystąpienie vertexu
                if(foundVericesIndices.size()>1){
                    averageNormal=averageNormal(normalsForAverage);

                    //uzyskaną wartość zapisz do wszystkich pozycji z outputNormalList wskazywanych przez odnalezione vertexy
                    for(int j: foundVericesIndices ){
                       outputNormalList.set(j,averageNormal);
                    }
                }

            }
        }
        //tu trzeba parsować obydwie listy do buforów i je przypisać do resultBuffers


        return resultBuffers;
    }

    private String averageNormal(List<String> normalsForAverage) {

        for(String normal: normalsForAverage){
            Log.d(TAG,normal);
        }

    return "";

    }


}

