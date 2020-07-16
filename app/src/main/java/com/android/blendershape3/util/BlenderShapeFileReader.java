package com.android.blendershape3.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.android.blendershape3.shapes.Shape;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.android.blendershape3.util.Constants.BYTES_PER_FLOAT;
import static com.android.blendershape3.util.Constants.BYTES_PER_SHORT;


public class BlenderShapeFileReader {

    private static final String TAG = "BlenderShapeFileReader";
    private Context context;

    /**Default Constructor
     *
     * @param context
     */
    public BlenderShapeFileReader(Context context){
        this.context=context;
    }



    /**
     * Reads .obj file with vertex data (v), normals data (vn), indexed vertex faces data (f)
     * @param filename .obj file
     * @return Set of 3 Buffers encapsulated in BufferObject for glDrawElements method if this
     * is *VN.obj file. Set of 2 Buffers encapsulated in BufferObject for glDrawTriangles if
     * this is original Blender *.obj file.
     *
     */
    public BufferObject getBuffers(String filename, int mode){

        BufferObject result;

        List[] fileContent=scanFile(filename);

        List<String> sourceVertexList=fileContent[0];
        List<String> sourceNormalList=fileContent[1];
        List<String> sourceFacesList=fileContent[2];

        List<Float> vertexFloatList;
        List<Float> normalFloatList;
        List<Short> facesShortList;

        if(mode== Shape.VERTEX_NORMALS) {

            vertexFloatList = getFloatList(sourceVertexList);
            normalFloatList = getFloatList(sourceNormalList);
            facesShortList = getShortList(sourceFacesList);

            FloatBuffer outputVertexBuffer=ByteBuffer.allocateDirect(vertexFloatList.size()*BYTES_PER_FLOAT).
                    order(ByteOrder.nativeOrder()).asFloatBuffer();
            outputVertexBuffer.position(0);
            for(float value: vertexFloatList){
                outputVertexBuffer.put(value);
            }
            outputVertexBuffer.position(0);

            FloatBuffer outputNormalBuffer=ByteBuffer.allocateDirect(normalFloatList.size()*BYTES_PER_FLOAT).
                    order(ByteOrder.nativeOrder()).asFloatBuffer();
            outputNormalBuffer.position(0);
            for(float value: normalFloatList){
                outputNormalBuffer.put(value);
            }
            outputNormalBuffer.position(0);

            ShortBuffer outputFacesBuffer=ByteBuffer.allocateDirect(facesShortList.size()*BYTES_PER_SHORT).
                    order(ByteOrder.nativeOrder()).asShortBuffer();
            outputFacesBuffer.position(0);
            for(short value: facesShortList){
                outputFacesBuffer.put(value);
            }
            outputFacesBuffer.position(0);

            result=new BufferObject(outputVertexBuffer,outputNormalBuffer,outputFacesBuffer);

        }else { //FACES_NORMALS

            FloatBuffer[] buffers=getFacesNormalsBuffers(sourceVertexList,sourceNormalList,sourceFacesList);
            result=new BufferObject(buffers[0],buffers[1],null);
        }


        return result;
    }



    /**
     * @param vertexList
     * @param normalsList
     * @param facesList
     * @return
     */
    private FloatBuffer[] getFacesNormalsBuffers(List<String> vertexList, List<String> normalsList, List<String> facesList) {
        FloatBuffer[] resultBuffers = new FloatBuffer[2];

        List<String> outputVertexList = new ArrayList<>();
        List<String> outputNormalList = new ArrayList<>();

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
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(outputVertexList.size() *
                3 * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
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
        FloatBuffer normalsBuffer = ByteBuffer.allocateDirect(outputNormalList.size() *
                3 * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
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


    /**
     * @param vertexList             Original, Blender generated vertex list
     * @param normalsList            Original, Blender generated normal list
     * @param facesList              Original, Blender generated faces list in format: V//N
     * @return
     */
    private FloatBuffer[] getVertexNormalsBuffers(List<String> vertexList, List<String> normalsList, List<String> facesList) {

        List<String> outputVertexList = new ArrayList<>();
        List<String> outputNormalList = new ArrayList<>();

        List<String> normalsForAverage;
        List<Integer> foundVerticesIndices;

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

        //wynikowa lista normalnych, wypełniona zarami dla zbudowania przestrzeni
        float[] zeroBuff = new float[1];
        List<float[]> outputNormalListFloat = new ArrayList<>();
        for (int i = 0; i < outputNormalList.size(); i++) {
            outputNormalListFloat.add(zeroBuff);
        }


        //po oryginalnej liście vertexów można znaleźć wszystkie wystąpienia danego Vertexu
        for (String vertex : vertexList) {
            normalsForAverage = new ArrayList<>();
            foundVerticesIndices = new ArrayList<>();

            for (int i = 0; i < outputVertexList.size(); i++) {

                if (outputVertexList.get(i).equals(vertex)) {
                    //dodaj normalną do uśrednienia
                    String tmpNormal = outputNormalList.get(i);
                    normalsForAverage.add(outputNormalList.get(i));
//                    Log.d(TAG,outputNormalList.get(i));

                    //dodaj index znalezionego Vertexu aby uśrednioną normalną wpisac do normalList w odpowiednim miejscu
                    foundVerticesIndices.add(i);

                }
            }
            //uśrednij wszystkie znalezione normalne

            float[] averNormal = averageNormal(normalsForAverage);

            //uzyskaną wartość zapisz do wszystkich pozycji z outputNormalList wskazywanych przez odnalezione vertexy
            for (int j : foundVerticesIndices) {
                outputNormalListFloat.set(j, averNormal);
            }


        }

        //outputNormalListFloat zawiera wszystkie normalne w postaci wektorów float[3] w kolejności
        // zgodnej z outputNormalList i outputVertexList


        //parsing vertices lines into floats

        //Buffer containing all vertices in float coordinates
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(outputVertexList.size() *
                3 * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
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

        //przepisuję wszytkie wyliczone normalne do bufora wynikowego
        //This buffer will contain all normal vectors in the same order as vertices (above)
        FloatBuffer normalsBuffer = ByteBuffer.allocateDirect(outputNormalList.size() *
                3 * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalsBuffer.position(0);

        for (float[] normal : outputNormalListFloat) {
            normalsBuffer.put(normal);
        }
        normalsBuffer.position(0);

        resultBuffers[0] = vertexBuffer;
        resultBuffers[1] = normalsBuffer;


        return resultBuffers;
    }




    /**
     * Wylicza Wektor średni wszystkich wektorów podanych w postaci Listy Stringów
     * eliminując wcześniej duplikaty
     *
     * @param normalsForAverage
     * @return znormalizowany wektor uśredniony (wersor) w formacie float[]
     */
    private float[] averageNormal(List<String> normalsForAverage) {

        //najpierw trzeba wyeliminować duplikaty, aby nie zaburzały średniej
        List<String> noDoubledNormalsList = new ArrayList<>();
        for (String line : normalsForAverage) {
            if (!noDoubledNormalsList.contains(line)) {
                noDoubledNormalsList.add(line);
            }
        }

        List<float[]> listOfNormalsToAverage = new ArrayList<>();

        float[] normalf;
        for (String line : noDoubledNormalsList) {
            normalf = new float[3];
            String[] normals = line.split(" ");  //normals[0]="vn"
            normalf[0] = Float.parseFloat(normals[1]);
            normalf[1] = Float.parseFloat(normals[2]);
            normalf[2] = Float.parseFloat(normals[3]);
            listOfNormalsToAverage.add(normalf);
        }

        //sumowanie wektorów
        float[] resultNormalF = new float[3];
        for (float[] vector : listOfNormalsToAverage) {
            resultNormalF[0] += vector[0];
            resultNormalF[1] += vector[1];
            resultNormalF[2] += vector[2];
        }
        resultNormalF[0] /= listOfNormalsToAverage.size();
        resultNormalF[1] /= listOfNormalsToAverage.size();
        resultNormalF[2] /= listOfNormalsToAverage.size();

        //normalizacja wektora wynikowego
        float length = (float) Math.sqrt((resultNormalF[0] * resultNormalF[0]) + (resultNormalF[1] * resultNormalF[1]) + (resultNormalF[2] * resultNormalF[2]));
        resultNormalF[0] /= length;
        resultNormalF[1] /= length;
        resultNormalF[2] /= length;


        return resultNormalF;

    }

    /**
     * Writes to file in External Storage/Downloads: vertex data, vertex normals data instead of faces normal,
     * and faces indexed vertex data for use by glDrawElements. Ads "VN" to filename.
     *
     *
     * @param filename
     */
    public void writeShapeVNToFile(String filename) {

        //check if this is Blender file
        if(!isBlenderFile(filename)){
            Log.e(TAG,"file: "+filename+" is not Blender file!");
            try {
                throw new Exception("file: "+filename+" is not Blender file!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        List[] fileContent=scanFile(filename);
        List<String> sourceVertexList=fileContent[0];
        List<String> sourceNormalList=fileContent[1];
        List<String> sourceFacesList=fileContent[2];


        List<float[]> outputNormalListFloat = new ArrayList<>();

        //przetważam źródłowe listy na bufory
        FloatBuffer[] buffers = getVertexNormalsBuffers(sourceVertexList, sourceNormalList, sourceFacesList);
        FloatBuffer vertexBuffer = buffers[0];
        FloatBuffer normalBuffer = buffers[1];
        //oba bufory mają tą samą długość i odpowiedającą sobie sekwencję

        //tworzę wejściową listę vertexów w postaci listy koordynatów float[]
        List<float[]> vfl = toFloatList(sourceVertexList);

        float[] normal;
        //przewijam po tej liście
        for (float[] vertex : vfl) {

            //wyszukuję w vertexBuffer pozycję, na której jest zapisany dany vertex
            for (int i = 0; i < vertexBuffer.capacity(); i += 3) {
                //porównuje trzy kolejne wartości
                if (vertex[0] == vertexBuffer.get(i) && vertex[1] == vertexBuffer.get(i + 1) && vertex[2] == vertexBuffer.get(i + 2)) {
                    normal = new float[]{
                            normalBuffer.get(i),
                            normalBuffer.get(i + 1),
                            normalBuffer.get(i + 2)
                    };
                    outputNormalListFloat.add(normal);
                    break;
                }
            }
        }
        //przepisuje liste normalnych do listy Stringów
        List<String> outputNormalList = toStringList(outputNormalListFloat);

        //każdy wiersz musi sie zaczynać od "vn "
        for (String line : outputNormalList) {
            String newLine = "vn " + line;
            outputNormalList.set(outputNormalList.indexOf(line), newLine);
        }

        //pozostaje przetworzyć listę Faces
        List<String> outputFacesList = new ArrayList<>();

        for (String line : sourceFacesList) {
            //split line by " "
            String combinedVI[] = line.split(" "); //combinedVI[0]="f"

            //są 4 elementy: f 5//1 3//1 1//1
            String complet1[] = combinedVI[1].split("//"); //returns 2 elements: complet1[0] is face vertex index
            String complet2[] = combinedVI[2].split("//");    //and complet1[1] is normal index
            String complet3[] = combinedVI[3].split("//");

            String outputLine = "f " + complet1[0] + " " + complet2[0] + " " + complet3[0];
            outputFacesList.add(outputLine);
        }

        //mozna pisać do pliku

        //add VN to filename
        String[] filenameSeq=filename.split("\\.");
        String newFilename=filenameSeq[0]+"VN."+filenameSeq[1];


        String sBody = "# Blender Shape with vertex normals \n";
        sBody+="# VN\n";
        sBody += "o " + filenameSeq[0] + "\n";
        for (String line : sourceVertexList) {
            sBody += line + "\n";
        }
        for (String line : outputNormalList) {
            sBody += line + "\n";
        }
        for (String line : outputFacesList) {
            sBody += line + "\n";
        }

//        Log.d(TAG,sBody);
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {

            try {
                Log.e(TAG, "If it isn't mounted - we can't write into it.");
                throw new Exception("Storage writing not allowed");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        File root = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        Log.d(TAG, "External file system root: " + root);
        File dir = new File(root.getAbsolutePath());
        dir.mkdirs();
        File file = new File(dir, newFilename);
        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println(sBody);
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param list
     * @return
     */
    private List<String> toStringList(List<float[]> list) {
        List<String> result = new ArrayList<>();

        for (float[] line : list) {
            String strLine = "" + line[0] + " " + line[1] + " " + line[2];
            result.add(strLine);
        }
        return result;
    }


    /**
     * @param list
     * @return
     */
    private List<float[]> toFloatList(List<String> list) {
        List<float[]> result = new ArrayList<>();

        for (String line : list) {
            String coords[] = line.split(" "); // Split by space. coords[0]="v"
            float x = Float.parseFloat(coords[1]);
            float y = Float.parseFloat(coords[2]);
            float z = Float.parseFloat(coords[3]);

            float[] vertexFloatCoords = new float[]{
                    x, y, z};
            result.add(vertexFloatCoords);
        }
        return result;

    }

    /**
     * Checks if file has # VN Header
     * @param filename
     * @return
     */
    private boolean isVNFile(String filename){

        boolean result=false;
        try {
            Scanner scanner = new Scanner(context.getAssets().open(filename));
            scanner.nextLine();
            String line2=scanner.nextLine();
            if(line2.contains("# VN")){
                result=true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private boolean isBlenderFile(String filename) {
        boolean result=false;
        try {
            Scanner scanner = new Scanner(context.getAssets().open(filename));
            scanner.nextLine();
            String line2=scanner.nextLine();
            if(line2.contains("# www.blender.org")){
                result=true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;


    }


    /**
     * @param sourceList
     * @return
     */
    private List<Short> getShortList(List<String> sourceList) {
        List<Short> result=new ArrayList<>();
        for(String line: sourceList){
            String[] values=line.split(" "); // f 5 3 1

            short x=(short)(Short.parseShort(values[1])-1);
            short y=(short)(Short.parseShort(values[2])-1);
            short z=(short)(Short.parseShort(values[3])-1);
            result.add(x);
            result.add(y);
            result.add(z);
        }
        return result;
    }


    /**
     * @param sourceList
     * @return
     */
    private List<Float> getFloatList(List<String> sourceList) {
        List<Float> result=new ArrayList<>();
        for(String line: sourceList){
            String[] values=line.split(" "); // v 1.250000 0.000000 0.000000
            float x=Float.parseFloat(values[1]);
            float y=Float.parseFloat(values[2]);
            float z=Float.parseFloat(values[3]);
            result.add(x);
            result.add(y);
            result.add(z);
        }
        return result;
    }


    /**
     * @param filename
     * @return
     */
    private List<String>[] scanFile(String filename){

        List<String> sourceVertexList=new ArrayList<>();
        List<String> sourceNormalList=new ArrayList<>();
        List<String> sourceFacesList=new ArrayList<>();

        try {
            Scanner scanner = new Scanner(context.getAssets().open(filename));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("v ")) {
                    sourceVertexList.add(line);
                } else if (line.startsWith("vn ")) {
                    sourceNormalList.add(line);
                } else if (line.startsWith("f ")) {
                    sourceFacesList.add(line);
                }
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new List[]{
                sourceVertexList,sourceNormalList,sourceFacesList
        };
    }

    private void logBuffers(FloatBuffer[] buffers) {
        FloatBuffer vBuffer = buffers[0];
        FloatBuffer nBuffer = buffers[1];
        for (int i = 0; i < vBuffer.capacity(); i++) {
            Log.d(TAG, "" + vBuffer.get(i) + "      " + nBuffer.get(i));
        }
    }

}

