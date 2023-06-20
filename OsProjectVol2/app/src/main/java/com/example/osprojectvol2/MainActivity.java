package com.example.osprojectvol2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Debug;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mFreeMemoryTextView;
    private TextView mUsedMemoryTextView;
    private ObjectPool<byte[]> mMemoryPool;
    private List<byte[]> mAllocatedMemory;
    private Stack<byte[]> mAvailableMemoryBlocks;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to the UI elements
        mFreeMemoryTextView = findViewById(R.id.free_memory_text_view);
        mUsedMemoryTextView = findViewById(R.id.used_memory_text_view);
        Button mAllocateMemoryButton = findViewById(R.id.allocate_memory_button);
        Button mFreeMemoryButton = findViewById(R.id.free_memory_button);
        pieChart = findViewById(R.id.chartpie);

        // Set click listeners for the buttons
        mAllocateMemoryButton.setOnClickListener(this);
        mFreeMemoryButton.setOnClickListener(this);

        // Initialize the allocated memory list, memory pool, and available memory blocks
        mAllocatedMemory = new ArrayList<>();
        int allocationSize = 1024 * 1024; // 1 MB
        mMemoryPool = new ObjectPool<>(10, () -> new byte[allocationSize]);
        mAvailableMemoryBlocks = new Stack<>();

        // Update the free memory text view
        long freeMemory = Runtime.getRuntime().freeMemory();
        updateFreeMemoryTextView(freeMemory);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.allocate_memory_button:
                // Acquire memory from the pool
                int numBlocksToAllocate = 5;
                for (int i = 0; i < numBlocksToAllocate; i++) {
                    byte[] memoryBlock = getUnusedMemoryBlock();
                    if (memoryBlock == null) {
                        memoryBlock = mMemoryPool.acquire();
                        mAllocatedMemory.add(memoryBlock);
                    }
                }
                break;

            case R.id.free_memory_button:
                // Release the allocated memory back to the pool
                for (byte[] memoryBlock : mAllocatedMemory) {
                    setMemoryBlockInUse(memoryBlock, false);
                    mMemoryPool.release(memoryBlock);
                    mAvailableMemoryBlocks.push(memoryBlock);
                }
                mAllocatedMemory.clear();
                break;
        }

        // Update the used and free memory text views
        long usedMemory = Debug.getNativeHeapAllocatedSize();
        long freeMemory = Runtime.getRuntime().freeMemory();
        updateUsedMemoryTextView(usedMemory);
        updateFreeMemoryTextView(freeMemory);

        // Update the PieChart
        updatePieChart();
    }

    private void updatePieChart() {
        // Get the updated used memory and free memory values
        long usedMemory = Debug.getNativeHeapAllocatedSize();
        long freeMemory = Runtime.getRuntime().freeMemory();

        // Calculate the total memory available+
        long totalMemory = Runtime.getRuntime().totalMemory();

        // Calculate the memory percentages
        double usedMemoryPercentage = (double) usedMemory / (double) totalMemory * 100;
        double freeMemoryPercentage = (double) freeMemory / (double) totalMemory * 100;

        // Update the PieChart
        pieChart.clearChart();
        pieChart.addPieSlice(new PieModel("Used Memory", (float) usedMemoryPercentage, getResources().getColor(R.color.purple_700)));
        pieChart.addPieSlice(new PieModel("Free Memory", (float) freeMemoryPercentage, getResources().getColor(R.color.customized)));
        pieChart.startAnimation();
    }

    private void updateFreeMemoryTextView(long freeMemory) {
        mFreeMemoryTextView.setText(getString(R.string.free_memory, freeMemory));
    }

    private void updateUsedMemoryTextView(long usedMemory) {
        mUsedMemoryTextView.setText(getString(R.string.used_memory, usedMemory));
    }



//    retrieving an unused memory block from the available memory blocks pool
    private byte[] getUnusedMemoryBlock() {
        if (!mAvailableMemoryBlocks.isEmpty()) {
            byte[] memoryBlock = mAvailableMemoryBlocks.pop();
            setMemoryBlockInUse(memoryBlock, true);
            return memoryBlock;
        }
        return null;
    }

    private boolean isMemoryBlockInUse(byte[] memoryBlock) {
        // Check if the memory block is in use by looking for it in the mAllocatedMemory list
        return mAllocatedMemory.contains(memoryBlock);
    }

    private void setMemoryBlockInUse(byte[] memoryBlock, boolean inUse) {
        // Mark the memory block as in use or not in use by setting a custom flag
        if (inUse) {
            memoryBlock[0] = 1;
        } else {
            memoryBlock[0] = 0;
        }
    }
}
