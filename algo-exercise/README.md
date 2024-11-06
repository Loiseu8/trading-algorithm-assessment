# Coding Black Females - Trading Algorithm Project by Toluwanimi Oladejo

Welcome to my trading algorithm project! This README provides an overview of the challenge, objectives, functionality, and key achievements realised during the development and testing of my algorithm.

## Project Overview

This project involves creating a trading algorithm that can analyse market data, make buy/sell decisions based on a technical indicator --- calculated Simple Moving Average (SMA) --- and manage risk through a stop-loss mechanism. The goal is to create a profitable trading system that adapts to different market conditions.

### Achievements
The algorithm successfully:
- **Calculates SMA** from recent bid prices for informed decision-making.
- **Executes Buy Orders** when conditions are favourable, maintaining up to 3 active orders.
- **Achieves Profitability** through sell orders triggered by profit targets, yielding cumulative gains.
- **Manages Risk** with a Stop-Loss mechanism to protect against adverse trends.
- **Adheres to Exit Conditions** that prevent excessive orders, ensuring stability.

These milestones align with the initial goal of creating a robust, profitable system capable of handling both bullish and bearish market conditions.

## Objectives

The core objective was to build a trading algorithm that dynamically places and cancels child orders. Additionally, a stretch goal was set to achieve profitability by buying shares at lower prices and selling at higher prices, generating a net profit over time.

### Key Functionalities

- **SMA Calculation**: The algorithm calculates an SMA based on recent bid prices, identifying favourable buy conditions.
- **Buy and Sell Logic**: Orders are placed based on market trends, buying when prices are favourable and selling when a profit target is achieved.
- **Stop-Loss Mechanism**: A risk management feature that cancels orders when prices fall below a set threshold, limiting potential losses.
- **Exit Condition**: Stops trading once 20 orders have been placed to prevent excessive risk-taking.

### Project Setup

#### Pre-requisites
- Java version 17 or higher.

  **Note**: If using a version above Java 17, you may see log warnings, which can be safely ignored.

#### How to Get Started
1. Clone this repository and open it as a Maven project in your IDE.
2. Run `./mvnw clean install` from the root directory to install necessary binaries for encoding and decoding.
3. Compile or test specific modules using Maven commands as needed:
    - Clean all projects: `./mvnw clean`
    - Compile `getting-started`: `./mvnw compile --projects algo-exercise/getting-started`

## Trading Algorithm Logic

This algorithm interacts with an order book and market data feed, creating a dynamic and responsive trading environment. The logic resides in the `MyAlgoLogic.java` class and is backtested via `MyAlgoBackTest.java`.

### Key Components and Workflow

- **Market Data Processing**: The algorithm receives and processes market data, using bid and ask prices to make decisions.
- **SMA-Based Buy Logic**: If the current bid price exceeds the SMA and fewer than 3 buy orders are active, a buy order is placed.
- **Profit-Taking with Sell Logic**: When the bid price reaches a profit target, the algorithm places a sell order to maximise gains.
- **Stop-Loss Logic**: To minimise loss during unfavourable conditions, a stop-loss mechanism cancels orders when prices fall too low.
- **Backtesting**: Through `MyAlgoBackTest.java`, various market conditions are simulated to validate the algorithm's performance and profitability.

### Backtesting Process

The backtesting framework integrates all aspects of the algorithm into a cohesive system, simulating real-world scenarios to assess the algorithm's effectiveness under different market trends. Backtests validate:

- **SMA-Triggered Buy Orders**: Ensuring correct placement and filling of buy orders.
- **Profit-Taking Sell Orders**: Validating profitability through sell actions.
- **Stop-Loss Effectiveness**: Testing how well the stop-loss mechanism limits downside risk.
- **Exit Condition**: Ensuring the algorithm respects the 20-order limit.

The results demonstrate the algorithm's robustness and ability to achieve profitable outcomes under diverse conditions.

## UI Front-End Exercise

As part of this project, a **Depth of Market (DOM) View** UI exercise was completed to simulate a visual interface for market data. The UI component was developed in `ui-front-end` using **React** and **TypeScript** and hosted with **Vite**. Although the UI is not yet integrated directly with the algorithm, it demonstrates the capability to display market depth information and track bid/ask levels dynamically, providing an enhanced user experience.

![UI Exercise Recording](ScreenRecording2024-10-22at11.28.52-ezgif.com-video-to-gif-converter.gif)

## Successes and Goals Achieved

Throughout the project, the following goals were achieved:
- **Profitability**: The algorithm meets the stretch goal of generating profits by capturing favourable buy/sell opportunities.
- **Risk Management**: Through the stop-loss mechanism, it successfully limits losses in bear markets.
- **Stability**: The exit condition ensures no more than 20 orders are placed, maintaining system stability.
- **Adaptability**: The SMA-based approach allows the algorithm to adapt to changing market conditions.

### Performance Logging

To ensure the algorithm operates efficiently, I implemented performance logging to track the execution time of key actions. This logging provides detailed insights into how long each part of the algorithm takes, enabling continuous performance monitoring and optimisation.

The following metrics are logged during each trading round:

* **SMA Calculation Time**: Measures the time taken to compute the Simple Moving Average (SMA) based on recent bid prices.
* **Buy Order Creation Time**: Tracks the time taken to place a buy order when conditions are favourable.
* **Sell Order Execution Time**: Logs the time required to execute a sell order once the profit target is reached.
* **Stop-Loss Execution Time**: Records the time taken to trigger a stop-loss and cancel orders to minimise losses.

In addition, my backtest includes an end-of-round summary outlining total execution time, order counts, and profit made, providing a comprehensive overview of the algorithm's efficiency.

## Future Improvements

- **Sell and Stop-Loss Unit Tests**: **Sell and Stop-Loss Unit Tests**: The sell and stop-loss unit tests currently do not pass as expected. At this time, I believe that this is due to the complexity of the algorithm logic, which may not be fully supported by the mechanisms provided in the baseline code for unit testing, as these mechanisms have all functioned efficiently in backtesting.
- **UI Integration**: Integrating the algorithm with the Depth of Market (DOM) UI component would provide a comprehensive view of market interactions, making the trading process more transparent and user-friendly.
- **Edge Case Handling**: Additional testing for rare or extreme market conditions.
- **Optimisation**: Fine-tuning the profit target and stop-loss values for better results.