import cv2
import numpy as np
import time

def calculate_running_speed(video_path, real_width=None, pixels_per_meter=None):
    """
    Calculate the running speed of a person in a video using optical flow.
    
    Args:
        video_path (str): Path to the video file
        real_width (float, optional): Real width of the frame in meters
        pixels_per_meter (float, optional): Conversion ratio from pixels to meters
        
    Returns:
        float: Average speed in meters per second
    """
    # Open the video
    cap = cv2.VideoCapture(video_path)
    
    # Check if video opened successfully
    if not cap.isOpened():
        print("Error: Could not open video.")
        return -1
    
    # Get video properties
    fps = cap.get(cv2.CAP_PROP_FPS)
    frame_width = cap.get(cv2.CAP_PROP_FRAME_WIDTH)
    
    # If pixels_per_meter is not provided but real_width is, calculate it
    if pixels_per_meter is None and real_width is not None:
        pixels_per_meter = frame_width / real_width
    # Default value if neither is provided (assuming 1 meter is 100 pixels)
    elif pixels_per_meter is None:
        pixels_per_meter = 100
        print("Warning: Using default pixels_per_meter value (100 pixels = 1 meter)")
    
    # Read the first frame
    ret, prev_frame = cap.read()
    if not ret:
        print("Error: Could not read the first frame.")
        return -1
    
    # Convert to grayscale
    prev_gray = cv2.cvtColor(prev_frame, cv2.COLOR_BGR2GRAY)
    
    # Create a mask for optical flow
    mask = np.zeros_like(prev_frame)
    
    # Variables to track movement
    total_displacement = 0
    frame_count = 0
    
    while True:
        # Read the next frame
        ret, frame = cap.read()
        if not ret:
            break
        
        # Convert to grayscale
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        
        # Calculate optical flow using Farneback method
        flow = cv2.calcOpticalFlowFarneback(prev_gray, gray, None, 0.5, 3, 15, 3, 5, 1.2, 0)
        
        # Calculate magnitude and angle of 2D vectors
        magnitude, angle = cv2.cartToPolar(flow[..., 0], flow[..., 1])
        
        # Filter out small movements (noise)
        significant_motion_mask = magnitude > 1.0
        
        # Calculate the average horizontal displacement (assuming person runs horizontally)
        if np.any(significant_motion_mask):
            horizontal_displacement = np.mean(np.abs(flow[significant_motion_mask, 0]))
            total_displacement += horizontal_displacement
            frame_count += 1
        
        # Update the previous frame
        prev_gray = gray.copy()
        
        # Visualization (optional)
        # Draw the optical flow
        for i in range(0, flow.shape[0], 10):
            for j in range(0, flow.shape[1], 10):
                if significant_motion_mask[i, j]:
                    cv2.line(mask, (j, i), (j + int(flow[i, j, 0]), i + int(flow[i, j, 1])), (0, 255, 0), 2)
        
        result = cv2.add(frame, mask)
        cv2.imshow('Optical Flow', result)
        
        # Break the loop if 'q' is pressed
        if cv2.waitKey(30) & 0xFF == ord('q'):
            break
    
    # Release resources
    cap.release()
    cv2.destroyAllWindows()
    
    # Calculate average speed
    if frame_count > 0:
        avg_displacement_per_frame = total_displacement / frame_count
        avg_displacement_per_second = avg_displacement_per_frame * fps
        speed_meters_per_second = avg_displacement_per_second / pixels_per_meter
        return speed_meters_per_second
    else:
        return 0

if __name__ == "__main__":
    # Example usage
    video_path = "input.mp4"  # Replace with your video path
    
    # Option 1: If you know the real width of the frame in meters
    # speed = calculate_running_speed(video_path, real_width=5.0)
    
    # Option 2: If you know the pixels per meter ratio
    # speed = calculate_running_speed(video_path, pixels_per_meter=200)
    
    # Option 3: Using default values
    speed = calculate_running_speed(video_path)
    
    print(f"The estimated running speed is {speed:.2f} meters per second")
    print(f"This is equivalent to {speed * 3.6:.2f} km/h")
